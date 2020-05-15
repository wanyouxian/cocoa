package com.rocky.cocoa.server.query.dataframe;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.function.Function;

public interface AggregateFuction extends Function<Double[], Double> {

  public String shortName();

  public static class MaxAggregation implements AggregateFuction {

    @Override
    public Double apply(Double[] values) {
      if (values.length == 0) {
        return 0.0;
      }
      Double max = (Double) values[0];
      for (Double d : values) {
        double v = d.doubleValue();
        if (v > max) {
          max = v;
        }
      }
      return max;
    }

    @Override
    public String shortName() {
      return "max";
    }
  }

  public static class MinAggregation implements AggregateFuction {

    @Override
    public Double apply(Double[] values) {
      if (values.length == 0) {
        return 0.0;
      }
      Double min = (Double) values[0];
      for (Double d : values) {
        double v = d.doubleValue();
        if (v < min) {
          min = v;
        }
      }
      return min;
    }

    @Override
    public String shortName() {
      return "min";
    }
  }

  public static class AvgAggregation implements AggregateFuction {

    @Override
    public Double apply(Double[] values) {
      if (values.length == 0) {
        return 0.0d;
      }
      double sum = 0d;
      for (double d : values) {
        sum += d;
      }
      BigDecimal bdSum = new BigDecimal(sum + "");
      BigDecimal result = bdSum
          .divide(new BigDecimal(values.length + ""), 10, RoundingMode.HALF_UP);
      return Double.parseDouble(result.toString());
    }

    @Override
    public String shortName() {
      return "avg";
    }
  }

  public static class SumAggregation implements AggregateFuction {

    @Override
    public Double apply(Double[] values) {
      if (values.length == 0) {
        return 0.0;
      }
      double sum = 0;
      for (Double d : values) {
        sum += d.doubleValue();
      }
      return sum;
    }

    @Override
    public String shortName() {
      return "sum";
    }
  }

  public static class StdAggregation implements AggregateFuction {

    @Override
    public Double apply(Double[] values) {
      throw new RuntimeException("not implemented");
    }

    @Override
    public String shortName() {
      return "std";
    }
  }

}
