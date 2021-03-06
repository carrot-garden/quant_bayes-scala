package dk.bayes.infer.gp.gpr

import org.junit._
import org.junit.Assert._
import breeze.linalg.DenseVector
import breeze.optimize.LBFGS
import dk.bayes.math.linear._
import scala.math._
import dk.bayes.infer.gp.cov.CovSEiso
import dk.bayes.math.gaussian.MultivariateGaussian

/**
 * Learning gpml following http://www.gaussianprocess.org/gpml/code/matlab/doc/index.html regression example
 */
class GpmlRegressionLearnTest {

  //[x,y]
  private val data = loadCSV("src/test/resources/gpml/regression_data.csv", 1)
  private val x = data.column(0)
  private val y = data.column(1)

  @Test def test {

    // logarithm of [signal standard deviation,length-scale,likelihood noise standard deviation] 
    val initialParams = DenseVector(log(1d), log(1), log(0.1))
    val diffFunction = GpDiffFunction(x, y)

    val optimizer = new LBFGS[DenseVector[Double]](maxIter = 100, m = 3, tolerance = 1.0E-6)
    val optIterations = optimizer.iterations(diffFunction, initialParams).toList
    println(optIterations.last.x)
    assertEquals(15, optIterations.size)

    val newParams = optIterations.last.x
    //assert -negative log likelihood
    assertEquals(154.0689, diffFunction.calculate(initialParams)._1, 0.0001)
    assertEquals(14.1310, diffFunction.calculate(newParams)._1, 0.0001)

    assertEquals(0.68594, newParams(0), 0.0001)
    assertEquals(-0.99340, newParams(1), 0.0001)
    assertEquals(-1.9025, newParams(2), 0.0001)
    println("Learned gp parameters:" + newParams)
  }

  @Test def predict_given_learned_parameters {
    val covFunc = CovSEiso(sf = 0.68594, ell = -0.99340)
    val noiseStdDev = -1.9025

    val z = Matrix(Array(-1d, 1))
    val model = GenericGPRegression(x, y, covFunc, noiseStdDev)

    val predictions = model.predict(z)

    assertEquals(0.037, predictions(0, 0), 0.0001) //z(0) mean
    assertEquals(0.04051, predictions(0, 1), 0.0001) //z(0) variance

    assertEquals(0.9783, predictions(1, 0), 0.0001) //z(1) mean
    assertEquals(1.7361, predictions(1, 1), 0.0001) //z(1) variance
  }
}