package io.usethesource.vallang.basic;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import io.usethesource.vallang.INumber;
import io.usethesource.vallang.IReal;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import io.usethesource.vallang.IInteger;
import io.usethesource.vallang.IValueFactory;
import io.usethesource.vallang.Setup;

import static org.junit.Assert.assertTrue;

@RunWith(Parameterized.class)
public final class BigDecimalCalculationSmokeTest {

  @Parameterized.Parameters
  public static Iterable<? extends Object> data() {
    return Setup.valueFactories();
  }

  private final IValueFactory vf;

  public BigDecimalCalculationSmokeTest(final IValueFactory vf) {
    this.vf = vf;
  }

  private static void assertClose(INumber param, IReal actual, double expected) {
    assertClose(param, actual, expected, 6);
  }

  private static void assertClose(INumber param, IReal actual, double expected,
      int significantDigits) {
    long order = 0;

    if (Math.abs(expected) > 0.00001) {
      order = Math.round(Math.floor(Math.log10(Math.abs(expected))));
    }

    double maxError = Math.pow(10, order - significantDigits);

    assertTrue("failed for " + param + " real:" + actual + " double: " + expected,
        Math.abs(actual.doubleValue() - expected) < maxError);
  }

  @Test
  public void testSinComparableToFloatingPoint() {
    IReal start = vf.real(-100);
    IReal stop = start.negate();
    IReal increments = vf.real("0.1");
    for (IReal param = start; !stop.less(param).getValue(); param = param.add(increments)) {
      assertClose(param, param.sin(vf.getPrecision()), Math.sin(param.doubleValue()));
    }
  }

  @Test
  public void testCosComparableToFloatingPoint() {
    IReal start = vf.real(-100);
    IReal stop = start.negate();
    IReal increments = vf.real("0.1");
    for (IReal param = start; !stop.less(param).getValue(); param = param.add(increments)) {
      assertClose(param, param.cos(vf.getPrecision()), Math.cos(param.doubleValue()));
    }
  }

  @Test
  public void testTanComparableToFloatingPoint() {
    IReal start = vf.pi(vf.getPrecision()).divide(vf.real(2.0), vf.getPrecision()).negate();
    IReal stop = start.negate();
    IReal increments = vf.real("0.01");

    // around pi/2 tan is undefined so we skip checking around that.
    start = start.add(increments);
    stop = stop.subtract(increments);
    for (IReal param = start; !stop.less(param).getValue(); param = param.add(increments)) {
      assertClose(param, param.tan(vf.getPrecision()), Math.tan(param.doubleValue()));
    }
  }

  private static double log2(double x) {
    return Math.log(x) / Math.log(2);
  }

  @Test
  public void testLog2ComparableToFloatingPoint() {
    IReal start = vf.real(0);
    IReal stop = vf.real(100);
    IReal increments = vf.real("0.1");
    start = start.add(increments);
    for (IReal param = start; !stop.less(param).getValue(); param = param.add(increments)) {
      assertClose(param, param.log(vf.integer(2), vf.getPrecision()), log2(param.doubleValue()));
    }
  }

  @Test
  public void testLog10ComparableToFloatingPoint() {
    IReal start = vf.real(0);
    IReal stop = vf.real(100);
    IReal increments = vf.real("0.1");
    start = start.add(increments);
    for (IReal param = start; !stop.less(param).getValue(); param = param.add(increments)) {
      assertClose(param, param.log(vf.integer(10), vf.getPrecision()),
          Math.log10(param.doubleValue()));
    }
  }

  @Test
  public void testLnComparableToFloatingPoint() {
    IReal start = vf.real(0);
    IReal stop = vf.real(100);
    IReal increments = vf.real("0.1");
    start = start.add(increments);
    for (IReal param = start; !stop.less(param).getValue(); param = param.add(increments)) {
      assertClose(param, param.ln(vf.getPrecision()), Math.log(param.doubleValue()));
    }
  }

  @Test
  public void testPowAllNumbers() {
    IReal start = vf.real(-10);
    IReal stop = start.negate();
    IReal increments = vf.real("0.1");
    IReal x = vf.pi(10);

    for (IReal param = start; !stop.less(param).getValue(); param = param.add(increments)) {
      assertClose(param, x.pow(param, vf.getPrecision()),
          Math.pow(x.doubleValue(), param.doubleValue()));
    }
  }

  @Test
  public void testPowNaturalNumbers() {
    IInteger start = vf.integer(-10);
    IInteger stop = start.negate();
    IInteger increments = vf.integer(1);
    IReal x = vf.pi(10);

    for (IInteger param = start; !stop.less(param).getValue(); param = param.add(increments)) {
      assertClose(param, x.pow(param), Math.pow(x.doubleValue(), param.doubleValue()));
    }
  }

  @Test
  public void testExpComparableToFloatingPoint() {
    IReal start = vf.real(-100);
    IReal stop = start.negate();
    IReal increments = vf.real("0.1");
    for (IReal param = start; !stop.less(param).getValue(); param = param.add(increments)) {
      assertClose(param, param.exp(vf.getPrecision()), Math.exp(param.doubleValue()));
    }
  }

  private void assertTakesLessThan(final int seconds, String call, final Runnable x) {
    final Semaphore done = new Semaphore(0);
    Thread t = new Thread(new Runnable() {
      @Override
      public void run() {
        try {
          x.run();
        } finally {
          done.release();
        }
      }
    });
    try {
      t.start();
      if (!done.tryAcquire(seconds, TimeUnit.SECONDS)) {
        t.interrupt();
        assertTrue(call + " took more than 2 second.", false);
      }
    } catch (InterruptedException e) {
    }
  }

  @Test
  public void testExpPerformance() {
    // exp(x) is small for negative x
    IReal start = vf.pi(20).multiply(vf.real(10));
    IReal stop = start.subtract(start.multiply(vf.real(100)));
    IReal increments = vf.real(1);
    for (IReal param = start; stop.less(param).getValue(); param = param.subtract(increments)) {
      final IReal currentParam = param;
      assertTakesLessThan(2, "exp(" + param + ")", new Runnable() {
        @Override
        public void run() {
          currentParam.exp(vf.getPrecision());
        }
      });
    }

  }

  @Test
  public void testLnPerformance() {
    // ln(x) is small for low x
    IReal start = vf.pi(50).multiply(vf.real(10).pow(vf.integer(8)));
    IReal stop = start.divide(vf.real(10).pow(vf.integer(30)), vf.getPrecision());
    IReal increments = vf.real(10);
    for (IReal param = start; stop.less(param).getValue(); param =
        param.divide(increments, vf.getPrecision())) {
      final IReal currentParam = param;
      assertTakesLessThan(2, "ln(" + param + ")", new Runnable() {
        @Override
        public void run() {
          currentParam.ln(vf.getPrecision());
        }
      });
    }
  }

}
