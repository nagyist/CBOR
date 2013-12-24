package com.upokecenter.util;
/*
Written in 2013 by Peter O.
Any copyright is dedicated to the Public Domain.
http://creativecommons.org/publicdomain/zero/1.0/
If you like this, you should donate to Peter O.
at: http://peteroupc.github.io/CBOR/
 */





    /**
     * Represents an arbitrary-precision decimal floating-point number.
     * Consists of an integer mantissa and an integer exponent, both arbitrary-precision.
     * The value of the number is equal to mantissa * 10^exponent.
     */
  public final class ExtendedDecimal implements Comparable<ExtendedDecimal> {
    BigInteger exponent;
    BigInteger mantissa;
    int flags;
    
    /**
     * Gets this object's exponent. This object's value will be an integer
     * if the exponent is positive or zero.
     */
    public BigInteger getExponent() { return exponent; }
    /**
     * Gets this object's unscaled value.
     */
    public BigInteger getMantissa() { return mantissa; }

    /**
     * 
     */
public BigInteger getSignedMantissa() { return this.isNegative() ? -mantissa : mantissa; }

    
    /**
     * Determines whether this object's mantissa and exponent are equal
     * to those of another object.
     * @param otherValue An ExtendedDecimal object.
     */
    public boolean EqualsInternal(ExtendedDecimal otherValue) {
      if (otherValue == null)
        return false;
      return this.exponent.equals(otherValue.exponent) &&
        this.mantissa.equals(otherValue.mantissa) &&
        this.flags==otherValue.flags;
    }
    
    
    /**
     * 
     * @param other A ExtendedDecimal object.
     */
    public boolean equals(ExtendedDecimal other) {
      return EqualsInternal(other);
    }
    /**
     * Determines whether this object's mantissa and exponent are equal
     * to those of another object and that other object is a decimal fraction.
     * @param obj A object object.
     * @return True if the objects are equal; false otherwise.
     */
    @Override public boolean equals(Object obj) {
      return EqualsInternal(((obj instanceof ExtendedDecimal) ? (ExtendedDecimal)obj : null));
    }
    /**
     * Calculates this object's hash code.
     * @return This object's hash code.
     */
    @Override public int hashCode() {
      int hashCode_ = 0;
      {
        hashCode_ = hashCode_ + 1000000007 * exponent.hashCode();
        hashCode_ = hashCode_ + 1000000009 * mantissa.hashCode();
        hashCode_ = hashCode_ + 1000000009 * flags;
      }
      return hashCode_;
    }
    
    /**
     * Creates a decimal fraction with the value exponent*10^mantissa.
     * @param mantissa The unscaled value.
     * @param exponent The decimal exponent.
     */
    public ExtendedDecimal (BigInteger mantissa, BigInteger exponent) {
      this.exponent = exponent;
      int sign=mantissa.signum();
      this.mantissa = sign<0 ? -mantissa : mantissa;
      this.flags=(sign<0) ? BigNumberFlags.FlagNegative : 0;
    }
    
    private static final int MaxSafeInt = 214748363;
    
    /**
     * Creates a decimal fraction from a string that represents a number.
     * <p> The format of the string generally consists of:<ul> <li> An optional
     * '-' or '+' character (if '-', the value is negative.)</li> <li> One
     * or more digits, with a single optional decimal point after the first
     * digit and before the last digit.</li> <li> Optionally, E+ (positive
     * exponent) or E- (negative exponent) plus one or more digits specifying
     * the exponent.</li> </ul> </p> <p> The format generally follows the
     * definition in java.math.BigDecimal(), except that the digits must
     * be ASCII digits ('0' through '9').</p>
     * @param str A string that represents a number.
     */
    public static ExtendedDecimal FromString(String str) {
      if (str == null)
        throw new NullPointerException("str");
      if (str.length() == 0)
        throw new NumberFormatException();
      int offset = 0;
      boolean negative = false;
      if (str.charAt(0) == '+' || str.charAt(0) == '-') {
        negative = (str.charAt(0) == '-');
        offset++;
      }
      int mantInt=0;
      FastInteger mant = null;
      boolean haveDecimalPoint = false;
      boolean haveDigits = false;
      boolean haveExponent = false;
      int newScaleInt=0;
      FastInteger newScale = null;
      int i = offset;
      if(i+8==str.length()){
        if((str.charAt(i)=='I' || str.charAt(i)=='i') &&
           (str.charAt(i+1)=='N' || str.charAt(i+1)=='n') &&
           (str.charAt(i+2)=='F' || str.charAt(i+2)=='f') &&
           (str.charAt(i+3)=='I' || str.charAt(i+3)=='i') &&
           (str.charAt(i+4)=='N' || str.charAt(i+4)=='n') &&
           (str.charAt(i+5)=='I' || str.charAt(i+5)=='i') &&
           (str.charAt(i+6)=='T' || str.charAt(i+6)=='t') &&
           (str.charAt(i+7)=='Y' || str.charAt(i+7)=='y'))
          return (negative) ? NegativeInfinity : PositiveInfinity;
      }
      if(i+3==str.length()){
        if((str.charAt(i)=='I' || str.charAt(i)=='i') &&
           (str.charAt(i+1)=='N' || str.charAt(i+1)=='n') &&
           (str.charAt(i+2)=='F' || str.charAt(i+2)=='f'))
          return (negative) ? NegativeInfinity : PositiveInfinity;
      }
      if(i+3<=str.length()){
        // Quiet NaN
        if((str.charAt(i)=='N' || str.charAt(i)=='n') &&
           (str.charAt(i+1)=='A' || str.charAt(i+1)=='a') &&
           (str.charAt(i+2)=='N' || str.charAt(i+2)=='n')){
          if(i+3==str.length()){
            if(!negative)return NaN;
            return ExtendedDecimal.CreateWithFlags(
              BigInteger.ZERO,BigInteger.ZERO,
              (negative ? BigNumberFlags.FlagNegative : 0)|BigNumberFlags.FlagQuietNaN);
          }
          i+=3;
          for (; i < str.length(); i++) {
            if (str.charAt(i) >= '0' && str.charAt(i) <= '9') {
              int thisdigit = (int)(str.charAt(i) - '0');
              if(mantInt>MaxSafeInt){
                if(mant==null)
                  mant=new FastInteger(mantInt);
                mant.Multiply(10);
                mant.AddInt(thisdigit);
              } else {
                mantInt*=10;
                mantInt+=thisdigit;
              }
            } else {
              throw new NumberFormatException();
            }
          }
          BigInteger bigmant=(mant==null) ? (BigInteger.valueOf(mantInt)) : mant.AsBigInteger();
          return ExtendedDecimal.CreateWithFlags(
            bigmant,BigInteger.ZERO,
            (negative ? BigNumberFlags.FlagNegative : 0)|BigNumberFlags.FlagQuietNaN);
        }
      }
      if(i+4<=str.length()){
        // Signaling NaN
        if((str.charAt(i)=='S' || str.charAt(i)=='s') &&
           (str.charAt(i+1)=='N' || str.charAt(i+1)=='n') &&
           (str.charAt(i+2)=='A' || str.charAt(i+2)=='a') &&
           (str.charAt(i+3)=='N' || str.charAt(i+3)=='n')){
          if(i+4==str.length()){
            if(!negative)return SignalingNaN;
            return ExtendedDecimal.CreateWithFlags(
              BigInteger.ZERO,
              BigInteger.ZERO,BigNumberFlags.FlagSignalingNaN);
          }
          i+=4;
          for (; i < str.length(); i++) {
            if (str.charAt(i) >= '0' && str.charAt(i) <= '9') {
              int thisdigit = (int)(str.charAt(i) - '0');
              if(mantInt>MaxSafeInt){
                if(mant==null)
                  mant=new FastInteger(mantInt);
                mant.Multiply(10);
                mant.AddInt(thisdigit);
              } else {
                mantInt*=10;
                mantInt+=thisdigit;
              }
            } else {
              throw new NumberFormatException();
            }
          }
          BigInteger bigmant=(mant==null) ? (BigInteger.valueOf(mantInt)) : mant.AsBigInteger();
          return ExtendedDecimal.CreateWithFlags(
            bigmant,BigInteger.ZERO,
            (negative ? BigNumberFlags.FlagNegative : 0)|BigNumberFlags.FlagSignalingNaN);
        }
      }
      for (; i < str.length(); i++) {
        if (str.charAt(i) >= '0' && str.charAt(i) <= '9') {
          int thisdigit = (int)(str.charAt(i) - '0');
          if(mantInt>MaxSafeInt){
            if(mant==null)
              mant=new FastInteger(mantInt);
            mant.Multiply(10);
            mant.AddInt(thisdigit);
          } else {
            mantInt*=10;
            mantInt+=thisdigit;
          }
          haveDigits = true;
          if (haveDecimalPoint) {
            if(newScaleInt==Integer.MIN_VALUE){
              if(newScale==null)
                newScale=new FastInteger(newScaleInt);
              newScale.AddInt(-1);
            } else {
              newScaleInt--;
            }
          }
        } else if (str.charAt(i) == '.') {
          if (haveDecimalPoint)
            throw new NumberFormatException();
          haveDecimalPoint = true;
        } else if (str.charAt(i) == 'E' || str.charAt(i) == 'e') {
          haveExponent = true;
          i++;
          break;
        } else {
          throw new NumberFormatException();
        }
      }
      if (!haveDigits)
        throw new NumberFormatException();
      if (haveExponent) {
        FastInteger exp = null;
        int expInt=0;
        offset = 1;
        haveDigits = false;
        if (i == str.length()) throw new NumberFormatException();
        if (str.charAt(i) == '+' || str.charAt(i) == '-') {
          if (str.charAt(i) == '-') offset = -1;
          i++;
        }
        for (; i < str.length(); i++) {
          if (str.charAt(i) >= '0' && str.charAt(i) <= '9') {
            haveDigits = true;
            int thisdigit = (int)(str.charAt(i) - '0');
            if(expInt>MaxSafeInt){
              if(exp==null)
                exp=new FastInteger(expInt);
              exp.Multiply(10);
              exp.AddInt(thisdigit);
            } else {
              expInt*=10;
              expInt+=thisdigit;
            }
          } else {
            throw new NumberFormatException();
          }
        }
        if (!haveDigits)
          throw new NumberFormatException();
        if(offset>=0 && newScaleInt==0 && newScale==null && exp==null){
          newScaleInt=expInt;
        } else if(exp==null){
          if(newScale==null)
            newScale=new FastInteger(newScaleInt);
          if (offset < 0)
            newScale.SubtractInt(expInt);
          else
            newScale.AddInt(expInt);
        } else {
          if(newScale==null)
            newScale=new FastInteger(newScaleInt);
          if (offset < 0)
            newScale.Subtract(exp);
          else
            newScale.Add(exp);
        }
      } else if (i != str.length()) {
        throw new NumberFormatException();
      }
      return ExtendedDecimal.CreateWithFlags(
        (mant==null) ? (BigInteger.valueOf(mantInt)) : mant.AsBigInteger(),
        (newScale==null) ? (BigInteger.valueOf(newScaleInt)) : newScale.AsBigInteger(),
        negative ? BigNumberFlags.FlagNegative : 0);
    }
    
    private static final class DecimalMathHelper implements IRadixMathHelper<ExtendedDecimal> {

    /**
     * 
     */
      public int GetRadix() {
        return 10;
      }

    /**
     * 
     * @param value A ExtendedDecimal object.
     */
      public int GetSign(ExtendedDecimal value) {
        return value.signum();
      }

    /**
     * 
     * @param value A ExtendedDecimal object.
     */
      public BigInteger GetMantissa(ExtendedDecimal value) {
        return value.mantissa;
      }

    /**
     * 
     * @param value A ExtendedDecimal object.
     */
      public BigInteger GetExponent(ExtendedDecimal value) {
        return value.exponent;
      }

    /**
     * 
     * @param mantissa A BigInteger object.
     * @param e1 A BigInteger object.
     * @param e2 A BigInteger object.
     */
      public BigInteger RescaleByExponentDiff(BigInteger mantissa, BigInteger e1, BigInteger e2) {
        boolean negative = (mantissa.signum() < 0);
        if (mantissa.signum() == 0) return BigInteger.ZERO;
        FastInteger diff = FastInteger.FromBig(e1).SubtractBig(e2).Abs();
        if (diff.CanFitInInt32()) {
          mantissa=mantissa.multiply(DecimalFraction.FindPowerOfTen(diff.AsInt32()));
        } else {
          mantissa=mantissa.multiply(DecimalFraction.FindPowerOfTenFromBig(diff.AsBigInteger()));
        }
        return mantissa;
      }

    /**
     * 
     * @param mantissa A BigInteger object.
     * @param exponent A BigInteger object.
     */
      public ExtendedDecimal CreateNew(BigInteger mantissa, BigInteger exponent) {
        return new ExtendedDecimal(mantissa, exponent);
      }

    /**
     * 
     * @param lastDigit A 32-bit signed integer.
     * @param olderDigits A 32-bit signed integer.
     * @param bigint A BigInteger object.
     */
      public IShiftAccumulator CreateShiftAccumulatorWithDigits(BigInteger bigint, int lastDigit, int olderDigits) {
        return new DigitShiftAccumulator(bigint, lastDigit, olderDigits);
      }

    /**
     * 
     * @param bigint A BigInteger object.
     */
      public IShiftAccumulator CreateShiftAccumulator(BigInteger bigint) {
        return new DigitShiftAccumulator(bigint,0,0);
      }

    /**
     * 
     * @param numerator A BigInteger object.
     * @param denominator A BigInteger object.
     */
      public boolean HasTerminatingRadixExpansion(BigInteger numerator, BigInteger denominator) {
        // Simplify denominator based on numerator
        BigInteger gcd = numerator.gcd(denominator);
        denominator=denominator.divide(gcd);
        if (denominator.signum()==0)
          return false;
        // Eliminate factors of 2
        while (denominator.testBit(0)==false) {
          denominator=denominator.shiftRight(1);
        }
        // Eliminate factors of 5
        while(true){
          BigInteger bigrem;
          BigInteger bigquo;
BigInteger[] divrem=(denominator).divideAndRemainder(BigInteger.valueOf(5));
bigquo=divrem[0];
bigrem=divrem[1];
          if(bigrem.signum()!=0)
            break;
          denominator=bigquo;
        }
        return denominator.compareTo(BigInteger.ONE) == 0;
      }

    /**
     * 
     * @param bigint A BigInteger object.
     * @param power A FastInteger object.
     */
      public BigInteger MultiplyByRadixPower(BigInteger bigint, FastInteger power) {
        if (power.signum() <= 0) return bigint;
        if (bigint.signum()==0) return bigint;
        if(bigint.compareTo(BigInteger.ONE)!=0){
          if (power.CanFitInInt32()) {
            bigint=bigint.multiply(DecimalFraction.FindPowerOfTen(power.AsInt32()));
          } else {
            bigint=bigint.multiply(DecimalFraction.FindPowerOfTenFromBig(power.AsBigInteger()));
          }
          return bigint;
        } else {
          if (power.CanFitInInt32()) {
            return (DecimalFraction.FindPowerOfTen(power.AsInt32()));
          } else {
            return (DecimalFraction.FindPowerOfTenFromBig(power.AsBigInteger()));
          }
        }
      }
      
    /**
     * 
     * @param value A ExtendedDecimal object.
     */
      public int GetFlags(ExtendedDecimal value) {
        return value.flags;
      }
      
    /**
     * 
     * @param mantissa A BigInteger object.
     * @param exponent A BigInteger object.
     * @param flags A 32-bit signed integer.
     */
      public ExtendedDecimal CreateNewWithFlags(BigInteger mantissa, BigInteger exponent, int flags) {
        return CreateWithFlags(mantissa,exponent,flags);
      }
    /**
     * 
     */
      public int GetArithmeticSupport() {
        return BigNumberFlags.FiniteAndNonFinite;
      }
    /**
     * 
     * @param val A 32-bit signed integer.
     */
public ExtendedDecimal ValueOf(int val) {
        return FromInt64(val);
      }
    }

    private static boolean AppendString(StringBuilder builder, char c, FastInteger count) {
      if (count.CompareToInt(Integer.MAX_VALUE) > 0 || count.signum() < 0) {
        throw new UnsupportedOperationException();
      }
      int icount = count.AsInt32();
      for (int i = icount - 1; i >= 0; i--) {
        builder.append(c);
      }
      return true;
    }
    private String ToStringInternal(int mode) {
      // Using Java's rules for converting ExtendedDecimal
      // values to a String
      boolean negative=(this.flags&BigNumberFlags.FlagNegative)!=0;
      if((this.flags&BigNumberFlags.FlagInfinity)!=0){
        return negative ? "-Infinity" : "Infinity";
      }
      if((this.flags&BigNumberFlags.FlagSignalingNaN)!=0){
        if(this.mantissa.signum()==0)
          return negative ? "-sNaN" : "sNaN";
        return negative ?
          "-sNaN"+(this.mantissa).abs().toString() :
          "sNaN"+(this.mantissa).abs().toString();
      }
      if((this.flags&BigNumberFlags.FlagQuietNaN)!=0){
        if(this.mantissa.signum()==0)
          return negative ? "-NaN" : "NaN";
        return negative ?
          "-NaN"+(this.mantissa).abs().toString() :
          "NaN"+(this.mantissa).abs().toString();
      }
      String mantissaString = (this.mantissa).abs().toString();
      int scaleSign = -this.exponent.signum();
      if (scaleSign == 0)
        return negative ? "-"+mantissaString : mantissaString;
      boolean iszero = (this.mantissa.signum()==0);
      if (mode == 2 && iszero && scaleSign < 0) {
        // special case for zero in plain
        return negative ? "-"+mantissaString : mantissaString;
      }
      FastInteger sbLength = new FastInteger(mantissaString.length());
      FastInteger adjustedExponent = FastInteger.FromBig(this.exponent);
      FastInteger thisExponent = FastInteger.Copy(adjustedExponent);
      adjustedExponent.Add(sbLength).AddInt(-1);
      FastInteger decimalPointAdjust = new FastInteger(1);
      FastInteger threshold = new FastInteger(-6);
      if (mode == 1) { // engineering String adjustments
        FastInteger newExponent = FastInteger.Copy(adjustedExponent);
        boolean adjExponentNegative = (adjustedExponent.signum() < 0);
        int intphase = FastInteger.Copy(adjustedExponent).Abs().Mod(3).AsInt32();
        if (iszero && (adjustedExponent.compareTo(threshold) < 0 ||
                       scaleSign < 0)) {
          if (intphase == 1) {
            if (adjExponentNegative) {
              decimalPointAdjust.AddInt(1);
              newExponent.AddInt(1);
            } else {
              decimalPointAdjust.AddInt(2);
              newExponent.AddInt(2);
            }
          } else if (intphase == 2) {
            if (!adjExponentNegative) {
              decimalPointAdjust.AddInt(1);
              newExponent.AddInt(1);
            } else {
              decimalPointAdjust.AddInt(2);
              newExponent.AddInt(2);
            }
          }
          threshold.AddInt(1);
        } else {
          if (intphase == 1) {
            if (!adjExponentNegative) {
              decimalPointAdjust.AddInt(1);
              newExponent.AddInt(-1);
            } else {
              decimalPointAdjust.AddInt(2);
              newExponent.AddInt(-2);
            }
          } else if (intphase == 2) {
            if (adjExponentNegative) {
              decimalPointAdjust.AddInt(1);
              newExponent.AddInt(-1);
            } else {
              decimalPointAdjust.AddInt(2);
              newExponent.AddInt(-2);
            }
          }
        }
        adjustedExponent = newExponent;
      }
      if (mode == 2 || ((adjustedExponent.compareTo(threshold) >= 0 &&
                         scaleSign >= 0))) {
        if (scaleSign > 0) {
          FastInteger decimalPoint = FastInteger.Copy(thisExponent).Add(sbLength);
          int cmp = decimalPoint.CompareToInt(0);
          StringBuilder builder = null;
          if (cmp < 0) {
            FastInteger tmpFast=new FastInteger(mantissaString.length()).AddInt(6);
            builder = new StringBuilder(
              tmpFast.CompareToInt(Integer.MAX_VALUE)>0 ?
              Integer.MAX_VALUE : tmpFast.AsInt32());
            if(negative)builder.append('-');
            builder.append("0.");
            AppendString(builder, '0', FastInteger.Copy(decimalPoint).Negate());
            builder.append(mantissaString);
          } else if (cmp == 0) {
            if (!decimalPoint.CanFitInInt32())
              throw new UnsupportedOperationException();
            int tmpInt = decimalPoint.AsInt32();
            if (tmpInt < 0) tmpInt = 0;
            FastInteger tmpFast=new FastInteger(mantissaString.length()).AddInt(6);
            builder = new StringBuilder(
              tmpFast.CompareToInt(Integer.MAX_VALUE)>0 ?
              Integer.MAX_VALUE : tmpFast.AsInt32());
            if(negative)builder.append('-');
            builder.append(mantissaString,0,(0)+(tmpInt));
            builder.append("0.");
            builder.append(mantissaString,tmpInt,(tmpInt)+(mantissaString.length() - tmpInt));
          } else if (decimalPoint.CompareToInt(mantissaString.length()) > 0) {
            FastInteger insertionPoint = sbLength;
            if (!insertionPoint.CanFitInInt32())
              throw new UnsupportedOperationException();
            int tmpInt = insertionPoint.AsInt32();
            if (tmpInt < 0) tmpInt = 0;
            FastInteger tmpFast=new FastInteger(mantissaString.length()).AddInt(6);
            builder = new StringBuilder(
              tmpFast.CompareToInt(Integer.MAX_VALUE)>0 ?
              Integer.MAX_VALUE : tmpFast.AsInt32());
            if(negative)builder.append('-');
            builder.append(mantissaString,0,(0)+(tmpInt));
            AppendString(builder, '0',
                         FastInteger.Copy(decimalPoint).SubtractInt(builder.length()));
            builder.append('.');
            builder.append(mantissaString,tmpInt,(tmpInt)+(mantissaString.length() - tmpInt));
          } else {
            if (!decimalPoint.CanFitInInt32())
              throw new UnsupportedOperationException();
            int tmpInt = decimalPoint.AsInt32();
            if (tmpInt < 0) tmpInt = 0;
            FastInteger tmpFast=new FastInteger(mantissaString.length()).AddInt(6);
            builder = new StringBuilder(
              tmpFast.CompareToInt(Integer.MAX_VALUE)>0 ?
              Integer.MAX_VALUE : tmpFast.AsInt32());
            if(negative)builder.append('-');
            builder.append(mantissaString,0,(0)+(tmpInt));
            builder.append('.');
            builder.append(mantissaString,tmpInt,(tmpInt)+(mantissaString.length() - tmpInt));
          }
          return builder.toString();
        } else if (mode == 2 && scaleSign < 0) {
          FastInteger negscale = FastInteger.Copy(thisExponent);
          StringBuilder builder = new StringBuilder();
          if(negative)builder.append('-');
          builder.append(mantissaString);
          AppendString(builder, '0', negscale);
          return builder.toString();
        } else if(!negative){
          return mantissaString;
        } else {
          return "-"+mantissaString;
        }
      } else {
        StringBuilder builder = null;
        if (mode == 1 && iszero && decimalPointAdjust.CompareToInt(1) > 0) {
          builder = new StringBuilder();
          if(negative)builder.append('-');
          builder.append(mantissaString);
          builder.append('.');
          AppendString(builder, '0', FastInteger.Copy(decimalPointAdjust).AddInt(-1));
        } else {
          FastInteger tmp = FastInteger.Copy(decimalPointAdjust);
          int cmp = tmp.CompareToInt(mantissaString.length());
          if (cmp > 0) {
            tmp.SubtractInt(mantissaString.length());
            builder = new StringBuilder();
            if(negative)builder.append('-');
            builder.append(mantissaString);
            AppendString(builder, '0', tmp);
          } else if (cmp < 0) {
            // Insert a decimal point at the right place
            if (!tmp.CanFitInInt32())
              throw new UnsupportedOperationException();
            int tmpInt = tmp.AsInt32();
            if (tmp.signum() < 0) tmpInt = 0;
            FastInteger tmpFast=new FastInteger(mantissaString.length()).AddInt(6);
            builder = new StringBuilder(
              tmpFast.CompareToInt(Integer.MAX_VALUE)>0 ?
              Integer.MAX_VALUE : tmpFast.AsInt32());
            if(negative)builder.append('-');
            builder.append(mantissaString,0,(0)+(tmpInt));
            builder.append('.');
            builder.append(mantissaString,tmpInt,(tmpInt)+(mantissaString.length() - tmpInt));
          } else if (adjustedExponent.signum() == 0 && !negative) {
            return mantissaString;
          } else if (adjustedExponent.signum() == 0 && negative) {
            return "-"+mantissaString;
          } else {
            builder = new StringBuilder();
            if(negative)builder.append('-');
            builder.append(mantissaString);
          }
        }
        if (adjustedExponent.signum() != 0) {
          builder.append(adjustedExponent.signum() < 0 ? "E-" : "E+");
          adjustedExponent.Abs();
          StringBuilder builderReversed = new StringBuilder();
          while (adjustedExponent.signum() != 0) {
            int digit = FastInteger.Copy(adjustedExponent).Mod(10).AsInt32();
            // Each digit is retrieved from right to left
            builderReversed.append((char)('0' + digit));
            adjustedExponent.Divide(10);
          }
          int count = builderReversed.length();
          for (int i = 0; i < count; i++) {
            builder.append(builderReversed.charAt(count - 1 - i));
          }
        }
        return builder.toString();
      }
    }
    
    /**
     * 
     */
public DecimalFraction ToDecimalFraction() {
      if(IsNaN() || IsInfinity()){
        throw new ArithmeticException("Value is infinity or NaN");
      }
      return new DecimalFraction(this.getSignedMantissa(),exponent);
    }
    
    /**
     * Converts this value to an arbitrary-precision integer. Any fractional
     * part in this value will be discarded when converting to a big integer.
     */
    public BigInteger ToBigInteger() {
      int sign = this.getExponent().signum();
      if (sign == 0) {
        BigInteger bigmantissa = this.getSignedMantissa();
        return bigmantissa;
      } else if (sign > 0) {
        BigInteger bigmantissa = this.getSignedMantissa();
        bigmantissa=bigmantissa.multiply(DecimalFraction.FindPowerOfTenFromBig(this.getExponent()));
        return bigmantissa;
      } else {
        BigInteger bigmantissa = this.getSignedMantissa();
        BigInteger bigexponent = this.getExponent();
        bigexponent=bigexponent.negate();
        bigmantissa=bigmantissa.divide(DecimalFraction.FindPowerOfTenFromBig(bigexponent));
        return bigmantissa;
      }
    }
    /**
     * Converts this value to a 32-bit floating-point number. The half-even
     * rounding mode is used.
     * @return The closest 32-bit floating-point number to this value.
     * The return value can be positive infinity or negative infinity if
     * this value exceeds the range of a 32-bit floating point number.
     */
    public float ToSingle() {
      if(IsPositiveInfinity())
        return Float.POSITIVE_INFINITY;
      if(IsNegativeInfinity())
        return Float.NEGATIVE_INFINITY;
      throw new UnsupportedOperationException();
    }
    /**
     * Converts this value to a 64-bit floating-point number. The half-even
     * rounding mode is used.
     * @return The closest 64-bit floating-point number to this value.
     * The return value can be positive infinity or negative infinity if
     * this value exceeds the range of a 64-bit floating point number.
     */
    public double ToDouble() {
      if(IsPositiveInfinity())
        return Double.POSITIVE_INFINITY;
      if(IsNegativeInfinity())
        return Double.NEGATIVE_INFINITY;
      throw new UnsupportedOperationException();
    }
    /**
     * Creates a decimal fraction from a 32-bit floating-point number.
     * This method computes the exact value of the floating point number,
     * not an approximation, as is often the case by converting the number
     * to a string.
     * @param flt A 32-bit floating-point number.
     * @return A decimal fraction with the same value as "flt".
     * @throws ArithmeticException "flt" is infinity or not-a-number.
     */
    public static ExtendedDecimal FromSingle(float flt) {
      int value = Float.floatToRawIntBits(flt);
      boolean neg = ((value >> 31) != 0);
      int fpExponent = (int)((value >> 23) & 0xFF);
      int fpMantissa = value & 0x7FFFFF;
      if (fpExponent == 255){
        if(fpMantissa==0){
          return neg ? NegativeInfinity : PositiveInfinity;
        }
        // Treat high bit of mantissa as quiet/signaling bit
        boolean quiet=(fpMantissa&0x400000)!=0;
        fpMantissa&=0x3FFFFF;
        BigInteger info=BigInteger.valueOf(fpMantissa);
        info=info.subtract(BigInteger.ONE);
        if(info.signum()==0){
          return quiet ? NaN : SignalingNaN;
        } else {
          return CreateWithFlags(info,BigInteger.ZERO,
                                 (neg ? BigNumberFlags.FlagNegative : 0)|
                                 (quiet ? BigNumberFlags.FlagQuietNaN :
                                  BigNumberFlags.FlagSignalingNaN));
        }
      }
      if (fpExponent == 0) fpExponent++;
      else fpMantissa |= (1 << 23);
      if (fpMantissa == 0){
        return neg ? ExtendedDecimal.NegativeZero : ExtendedDecimal.Zero;
      }
      fpExponent -= 150;
      while ((fpMantissa & 1) == 0) {
        fpExponent++;
        fpMantissa >>= 1;
      }
      if (fpExponent == 0) {
        if (neg) fpMantissa = -fpMantissa;
        return ExtendedDecimal.FromInt64(fpMantissa);
      } else if (fpExponent > 0) {
        // Value is an integer
        BigInteger bigmantissa = BigInteger.valueOf(fpMantissa);
        bigmantissa=bigmantissa.shiftLeft(fpExponent);
        if (neg) bigmantissa=(bigmantissa).negate();
        return ExtendedDecimal.FromBigInteger(bigmantissa);
      } else {
        // Value has a fractional part
        BigInteger bigmantissa = BigInteger.valueOf(fpMantissa);
        bigmantissa=bigmantissa.multiply(DecimalFraction.FindPowerOfFive(-fpExponent));
        if (neg) bigmantissa=(bigmantissa).negate();
        return new ExtendedDecimal(bigmantissa, BigInteger.valueOf(fpExponent));
      }
    }
    
    public static ExtendedDecimal FromBigInteger(BigInteger bigint) {
      return new ExtendedDecimal(bigint,BigInteger.ZERO);
    }
    
    public static ExtendedDecimal FromInt64(long valueSmall) {
      BigInteger bigint=BigInteger.valueOf(valueSmall);
      return new ExtendedDecimal(bigint,BigInteger.ZERO);
    }

    /**
     * Creates a decimal fraction from a 64-bit floating-point number.
     * This method computes the exact value of the floating point number,
     * not an approximation, as is often the case by converting the number
     * to a string.
     * @param dbl A 64-bit floating-point number.
     * @return A decimal fraction with the same value as "dbl"
     * @throws ArithmeticException "dbl" is infinity or not-a-number.
     */
    public static ExtendedDecimal FromDouble(double dbl) {
      int[] value = Extras.DoubleToIntegers(dbl);
      int fpExponent = (int)((value[1] >> 20) & 0x7ff);
      boolean neg=(value[1]>>31)!=0;
      if (fpExponent == 2047){
        if((value[1]&0xFFFFF)==0 && value[0]==0){
          return neg ? NegativeInfinity : PositiveInfinity;
        }
        // Treat high bit of mantissa as quiet/signaling bit
        boolean quiet=(value[1]&0x80000)!=0;
        value[1]&=0x7FFFF;
        BigInteger info=FastInteger.WordsToBigInteger(value);
        info=info.subtract(BigInteger.ONE);
        if(info.signum()==0){
          return quiet ? NaN : SignalingNaN;
        } else {
          return CreateWithFlags(info,BigInteger.ZERO,
                                 (neg ? BigNumberFlags.FlagNegative : 0)|
                                 (quiet ? BigNumberFlags.FlagQuietNaN :
                                  BigNumberFlags.FlagSignalingNaN));
        }
      }
      value[1]&=0xFFFFF; // Mask out the exponent and sign
      if (fpExponent == 0) fpExponent++;
      else value[1]|=0x100000;
      if ((value[1]|value[0]) != 0) {
        fpExponent+=DecimalFraction.ShiftAwayTrailingZerosTwoElements(value);
      } else {
        return neg ? ExtendedDecimal.NegativeZero : ExtendedDecimal.Zero;
      }
      fpExponent -= 1075;
      BigInteger fpMantissaBig=FastInteger.WordsToBigInteger(value);
      if (fpExponent == 0) {
        if (neg) fpMantissaBig=fpMantissaBig.negate();
        return ExtendedDecimal.FromBigInteger(fpMantissaBig);
      } else if (fpExponent > 0) {
        // Value is an integer
        BigInteger bigmantissa = fpMantissaBig;
        bigmantissa=bigmantissa.shiftLeft(fpExponent);
        if (neg) bigmantissa=(bigmantissa).negate();
        return ExtendedDecimal.FromBigInteger(bigmantissa);
      } else {
        // Value has a fractional part
        BigInteger bigmantissa = fpMantissaBig;
        bigmantissa=bigmantissa.multiply(DecimalFraction.FindPowerOfFive(-fpExponent));
        if (neg) bigmantissa=(bigmantissa).negate();
        return new ExtendedDecimal(bigmantissa, BigInteger.valueOf(fpExponent));
      }
    }

    /**
     * Creates a decimal fraction from an arbitrary-precision binary floating-point
     * number.
     * @param bigfloat A bigfloat.
     */
    public static ExtendedDecimal FromBigFloat(BigFloat bigfloat) {
      if((bigfloat)==null)throw new NullPointerException("bigfloat");
      BigInteger bigintExp = bigfloat.getExponent();
      BigInteger bigintMant = bigfloat.getMantissa();
      if (bigintExp.signum()==0) {
        // Integer
        return ExtendedDecimal.FromBigInteger(bigintMant);
      } else if (bigintExp.signum() > 0) {
        // Scaled integer
        FastInteger intcurexp = FastInteger.FromBig(bigintExp);
        BigInteger bigmantissa = bigintMant;
        boolean neg = (bigmantissa.signum() < 0);
        if (neg) bigmantissa=(bigmantissa).negate();
        while (intcurexp.signum() > 0) {
          int shift = 512;
          if (intcurexp.CompareToInt(512) < 0) {
            shift = intcurexp.AsInt32();
          }
          bigmantissa=bigmantissa.shiftLeft(shift);
          intcurexp.AddInt(-shift);
        }
        if (neg) bigmantissa=(bigmantissa).negate();
        return ExtendedDecimal.FromBigInteger(bigmantissa);
      } else {
        // Fractional number
        BigInteger bigmantissa = bigintMant;
        BigInteger negbigintExp=(bigintExp).negate();
        bigmantissa=bigmantissa.multiply(DecimalFraction.FindPowerOfFiveFromBig(negbigintExp));
        return new ExtendedDecimal(bigmantissa, bigintExp);
      }
    }
    
    /**
     * Converts this value to a string.The format of the return value is exactly
     * the same as that of the java.math.BigDecimal.toString() method.
     * @return A string representation of this object.
     */
    @Override public String toString() {
      return ToStringInternal(0);
    }
    /**
     * Same as toString(), except that when an exponent is used it will be
     * a multiple of 3. The format of the return value follows the format of
     * the java.math.BigDecimal.toEngineeringString() method.
     */
    public String ToEngineeringString() {
      return ToStringInternal(1);
    }
    /**
     * Converts this value to a string, but without an exponent part. The
     * format of the return value follows the format of the java.math.BigDecimal.toPlainString()
     * method.
     */
    public String ToPlainString() {
      return ToStringInternal(2);
    }

    /**
     * Represents the number 1.
     */
    
    public static final ExtendedDecimal One = new ExtendedDecimal(BigInteger.ONE,BigInteger.ZERO);

    /**
     * Represents the number 0.
     */
    
    public static final ExtendedDecimal Zero = new ExtendedDecimal(BigInteger.ZERO,BigInteger.ZERO);
    
    public static final ExtendedDecimal NegativeZero = ExtendedDecimal.CreateWithFlags(
      BigInteger.ZERO,BigInteger.ZERO,BigNumberFlags.FlagNegative);
    /**
     * Represents the number 10.
     */
    
    public static final ExtendedDecimal Ten = new ExtendedDecimal(BigInteger.TEN,BigInteger.ZERO);

    //----------------------------------------------------------------

    private static ExtendedDecimal CreateWithFlags(BigInteger mantissa,
                                                   BigInteger exponent, int flags) {
      ExtendedDecimal ext=new ExtendedDecimal(mantissa,exponent);
      ext.flags=flags;
      return ext;
    }
    
    /**
     * 
     */
    public static final ExtendedDecimal NaN=CreateWithFlags(
      BigInteger.ZERO,
      BigInteger.ZERO,BigNumberFlags.FlagQuietNaN);
    /**
     * 
     */
    public static final ExtendedDecimal SignalingNaN=CreateWithFlags(
      BigInteger.ZERO,
      BigInteger.ZERO,BigNumberFlags.FlagSignalingNaN);
    /**
     * 
     */
    public static final ExtendedDecimal PositiveInfinity=CreateWithFlags(
      BigInteger.ZERO,
      BigInteger.ZERO,BigNumberFlags.FlagInfinity);
    /**
     * 
     */
    public static final ExtendedDecimal NegativeInfinity=CreateWithFlags(
      BigInteger.ZERO,
      BigInteger.ZERO,BigNumberFlags.FlagInfinity|BigNumberFlags.FlagNegative);
    
    /**
     * 
     */
    public boolean IsPositiveInfinity() {
      return (this.flags&(BigNumberFlags.FlagInfinity|BigNumberFlags.FlagNegative))==
        (BigNumberFlags.FlagInfinity|BigNumberFlags.FlagNegative);
    }
    
    /**
     * 
     */
    public boolean IsNegativeInfinity() {
      return (this.flags&(BigNumberFlags.FlagInfinity|BigNumberFlags.FlagNegative))==
        (BigNumberFlags.FlagInfinity);
    }
    
    /**
     * 
     */
    public boolean IsNaN() {
      return (this.flags&(BigNumberFlags.FlagQuietNaN|BigNumberFlags.FlagSignalingNaN))!=0;
    }

    /**
     * 
     */
public boolean IsInfinity() {
      return (this.flags&(BigNumberFlags.FlagInfinity))!=0;
    }

    /**
     * 
     */
public boolean isNegative() {
        return (this.flags&(BigNumberFlags.FlagNegative))!=0;
      }

    /**
     * 
     */
    public boolean IsQuietNaN() {
      return (this.flags&(BigNumberFlags.FlagQuietNaN))!=0;
    }

    /**
     * 
     */
    public boolean IsSignalingNaN() {
      return (this.flags&(BigNumberFlags.FlagSignalingNaN))!=0;
    }

    /**
     * Gets this value's sign: -1 if negative; 1 if positive; 0 if zero.
     */
    public int signum() {
        return mantissa.signum()==0 ? 0 : (((this.flags&BigNumberFlags.FlagNegative)!=0) ? -1 : 1);
      }
    /**
     * Gets whether this object's value equals 0.
     */
    public boolean isZero() {
        return mantissa.signum()==0;
      }
    /**
     * Gets the absolute value of this object.
     */
    public ExtendedDecimal Abs() {
      return Abs(null);
    }

    /**
     * Gets an object with the same value as this one, but with the sign reversed.
     */
    public ExtendedDecimal Negate() {
      return Negate(null);
    }

    /**
     * Divides this object by another decimal fraction and returns the result.
     * When possible, the result will be exact.
     * @param divisor The divisor.
     * @return The quotient of the two numbers.
     * @throws ArithmeticException Attempted to divide by zero.
     * @throws ArithmeticException The result would have a nonterminating
     * decimal expansion.
     */
    public ExtendedDecimal Divide(ExtendedDecimal divisor) {
      return Divide(divisor, PrecisionContext.ForRounding(Rounding.Unnecessary));
    }

    /**
     * Divides this object by another decimal fraction and returns a result
     * with the same exponent as this object (the dividend).
     * @param divisor The divisor.
     * @param rounding The rounding mode to use if the result must be scaled
     * down to have the same exponent as this value.
     * @return The quotient of the two numbers.
     * @throws ArithmeticException Attempted to divide by zero.
     * @throws ArithmeticException The rounding mode is Rounding.Unnecessary
     * and the result is not exact.
     */
    public ExtendedDecimal DivideToSameExponent(ExtendedDecimal divisor, Rounding rounding) {
      return DivideToExponent(divisor, this.exponent, PrecisionContext.ForRounding(rounding));
    }

    /**
     * Divides two ExtendedDecimal objects, and returns the integer part
     * of the result, rounded down, with the preferred exponent set to this
     * value's exponent minus the divisor's exponent.
     * @param divisor The divisor.
     * @return The integer part of the quotient of the two objects.
     * @throws ArithmeticException Attempted to divide by zero.
     */
    public ExtendedDecimal DivideToIntegerNaturalScale(
      ExtendedDecimal divisor
     ) {
      return DivideToIntegerNaturalScale(divisor, PrecisionContext.ForRounding(Rounding.Down));
    }

    /**
     * Removes trailing zeros from this object's mantissa. For example,
     * 1.000 becomes 1.
     * @param ctx A precision context to control precision, rounding, and
     * exponent range of the result. If HasFlags of the context is true, will
     * also store the flags resulting from the operation (the flags are in
     * addition to the pre-existing flags). Can be null.
     * @return This value with trailing zeros removed. Note that if the result
     * has a very high exponent and the context says to clamp high exponents,
     * there may still be some trailing zeros in the mantissa. If a precision
     * context is given, returns null if the result of rounding would cause
     * an overflow. The caller can handle a null return value by treating
     * it as positive or negative infinity depending on the sign of this object's
     * value.
     */
    public ExtendedDecimal Reduce(
      PrecisionContext ctx) {
      return math.Reduce(this, ctx);
    }
    /**
     * 
     * @param divisor A ExtendedDecimal object.
     */
    public ExtendedDecimal RemainderNaturalScale(
      ExtendedDecimal divisor
     ) {
      return RemainderNaturalScale(divisor,null);
    }

    /**
     * 
     * @param divisor A ExtendedDecimal object.
     * @param ctx A PrecisionContext object.
     */
    public ExtendedDecimal RemainderNaturalScale(
      ExtendedDecimal divisor,
      PrecisionContext ctx
     ) {
      return Subtract(this.DivideToIntegerNaturalScale(divisor,null)
                      .Multiply(divisor,null),ctx);
    }

    /**
     * Divides two ExtendedDecimal objects, and gives a particular exponent
     * to the result.
     * @param divisor A ExtendedDecimal object.
     * @param desiredExponentSmall The desired exponent. A negative number
     * places the cutoff point to the right of the usual decimal point. A positive
     * number places the cutoff point to the left of the usual decimal point.
     * @param ctx A precision context object to control the rounding mode
     * to use if the result must be scaled down to have the same exponent as
     * this value. The precision setting of this context is ignored. If HasFlags
     * of the context is true, will also store the flags resulting from the
     * operation (the flags are in addition to the pre-existing flags).
     * Can be null, in which case the default rounding mode is HalfEven.
     * @return The quotient of the two objects.
     * @throws ArithmeticException Attempted to divide by zero.
     * @throws ArithmeticException The desired exponent is outside of
     * the valid range of the precision context, if it defines an exponent
     * range.
     * @throws ArithmeticException The rounding mode is Rounding.Unnecessary
     * and the result is not exact.
     */
    public ExtendedDecimal DivideToExponent(
      ExtendedDecimal divisor,
      long desiredExponentSmall,
      PrecisionContext ctx
     ) {
      return DivideToExponent(divisor, (BigInteger.valueOf(desiredExponentSmall)), ctx);
    }

    /**
     * Divides two ExtendedDecimal objects, and gives a particular exponent
     * to the result.
     * @param divisor A ExtendedDecimal object.
     * @param desiredExponentSmall The desired exponent. A negative number
     * places the cutoff point to the right of the usual decimal point. A positive
     * number places the cutoff point to the left of the usual decimal point.
     * @param rounding The rounding mode to use if the result must be scaled
     * down to have the same exponent as this value.
     * @return The quotient of the two objects.
     * @throws ArithmeticException Attempted to divide by zero.
     * @throws ArithmeticException The rounding mode is Rounding.Unnecessary
     * and the result is not exact.
     */
    public ExtendedDecimal DivideToExponent(
      ExtendedDecimal divisor,
      long desiredExponentSmall,
      Rounding rounding
     ) {
      return DivideToExponent(divisor, (BigInteger.valueOf(desiredExponentSmall)), PrecisionContext.ForRounding(rounding));
    }

    /**
     * Divides two ExtendedDecimal objects, and gives a particular exponent
     * to the result.
     * @param divisor A ExtendedDecimal object.
     * @param exponent The desired exponent. A negative number places the
     * cutoff point to the right of the usual decimal point. A positive number
     * places the cutoff point to the left of the usual decimal point.
     * @param ctx A precision context object to control the rounding mode
     * to use if the result must be scaled down to have the same exponent as
     * this value. The precision setting of this context is ignored. If HasFlags
     * of the context is true, will also store the flags resulting from the
     * operation (the flags are in addition to the pre-existing flags).
     * Can be null, in which case the default rounding mode is HalfEven.
     * @return The quotient of the two objects.
     * @throws ArithmeticException Attempted to divide by zero.
     * @throws ArithmeticException The desired exponent is outside of
     * the valid range of the precision context, if it defines an exponent
     * range.
     * @throws ArithmeticException The rounding mode is Rounding.Unnecessary
     * and the result is not exact.
     */
    public ExtendedDecimal DivideToExponent(
      ExtendedDecimal divisor, BigInteger exponent, PrecisionContext ctx) {
      return math.DivideToExponent(this, divisor, exponent, ctx);
    }

    /**
     * Divides two ExtendedDecimal objects, and gives a particular exponent
     * to the result.
     * @param divisor A ExtendedDecimal object.
     * @param desiredExponent The desired exponent. A negative number
     * places the cutoff point to the right of the usual decimal point. A positive
     * number places the cutoff point to the left of the usual decimal point.
     * @param rounding The rounding mode to use if the result must be scaled
     * down to have the same exponent as this value.
     * @return The quotient of the two objects.
     * @throws ArithmeticException Attempted to divide by zero.
     * @throws ArithmeticException The rounding mode is Rounding.Unnecessary
     * and the result is not exact.
     */
    public ExtendedDecimal DivideToExponent(
      ExtendedDecimal divisor,
      BigInteger desiredExponent,
      Rounding rounding
     ) {
      return DivideToExponent(divisor, desiredExponent, PrecisionContext.ForRounding(rounding));
    }

    /**
     * Finds the absolute value of this object (if it's negative, it becomes
     * positive).
     * @param context A precision context to control precision, rounding,
     * and exponent range of the result. If HasFlags of the context is true,
     * will also store the flags resulting from the operation (the flags
     * are in addition to the pre-existing flags). Can be null.
     * @return The absolute value of this object.
     */
    public ExtendedDecimal Abs(PrecisionContext context) {
      return math.Abs(this,context);
    }

    /**
     * Returns a decimal fraction with the same value as this object but with
     * the sign reversed.
     * @param context A precision context to control precision, rounding,
     * and exponent range of the result. If HasFlags of the context is true,
     * will also store the flags resulting from the operation (the flags
     * are in addition to the pre-existing flags). Can be null.
     */
    public ExtendedDecimal Negate(PrecisionContext context) {
      return math.Negate(this,context);
    }

    /**
     * Adds this object and another decimal fraction and returns the result.
     * @param decfrac A ExtendedDecimal object.
     * @return The sum of the two objects.
     */
    public ExtendedDecimal Add(ExtendedDecimal decfrac) {
      if((decfrac)==null)throw new NullPointerException("decfrac");
      return Add(decfrac, PrecisionContext.Unlimited);
    }

    /**
     * Subtracts a ExtendedDecimal object from this instance and returns
     * the result..
     * @param decfrac A ExtendedDecimal object.
     * @return The difference of the two objects.
     */
    public ExtendedDecimal Subtract(ExtendedDecimal decfrac) {
      return Subtract(decfrac,null);
    }

    /**
     * Subtracts a ExtendedDecimal object from this instance.
     * @param decfrac A ExtendedDecimal object.
     * @param ctx A precision context to control precision, rounding, and
     * exponent range of the result. If HasFlags of the context is true, will
     * also store the flags resulting from the operation (the flags are in
     * addition to the pre-existing flags). Can be null.
     * @return The difference of the two objects. If a precision context
     * is given, returns null if the result of rounding would cause an overflow.
     */
    public ExtendedDecimal Subtract(ExtendedDecimal decfrac, PrecisionContext ctx) {
      if((decfrac)==null)throw new NullPointerException("decfrac");
      ExtendedDecimal negated=((decfrac.flags&BigNumberFlags.FlagNaN)!=0) ? decfrac :
        ExtendedDecimal.CreateWithFlags(
          decfrac.mantissa,decfrac.exponent,
          decfrac.flags^BigNumberFlags.FlagNegative);
      return Add(negated, ctx);
    }
    /**
     * Multiplies two decimal fractions. The resulting scale will be the
     * sum of the scales of the two decimal fractions.
     * @param decfrac Another decimal fraction.
     * @return The product of the two decimal fractions. If a precision context
     * is given, returns null if the result of rounding would cause an overflow.
     * A caller can handle a null return value by treating it as positive infinity
     * if both operands have the same sign or as negative infinity if both
     * operands have different signs.
     */
    public ExtendedDecimal Multiply(ExtendedDecimal decfrac) {
      if((decfrac)==null)throw new NullPointerException("decfrac");
      return Multiply(decfrac, PrecisionContext.Unlimited);
    }

    /**
     * Multiplies by one decimal fraction, and then adds another decimal
     * fraction.
     * @param multiplicand The value to multiply.
     * @param augend The value to add.
     * @return The result this * multiplicand + augend.
     */
    public ExtendedDecimal MultiplyAndAdd(ExtendedDecimal multiplicand,
                                          ExtendedDecimal augend) {
      return MultiplyAndAdd(multiplicand,augend,null);
    }
    //----------------------------------------------------------------

    private static RadixMath<ExtendedDecimal> math = new RadixMath<ExtendedDecimal>(
      new DecimalMathHelper());

    /**
     * Divides this object by another object, and returns the integer part
     * of the result, with the preferred exponent set to this value's exponent
     * minus the divisor's exponent.
     * @param divisor The divisor.
     * @param ctx A precision context object to control the precision, rounding,
     * and exponent range of the integer part of the result. Flags will be
     * set on the given context only if the context's HasFlags is true and
     * the integer part of the result doesn't fit the precision and exponent
     * range without rounding.
     * @return The integer part of the quotient of the two objects. Returns
     * null if the return value would overflow the exponent range. A caller
     * can handle a null return value by treating it as positive infinity
     * if both operands have the same sign or as negative infinity if both
     * operands have different signs.
     * @throws ArithmeticException Attempted to divide by zero.
     * @throws ArithmeticException The rounding mode is Rounding.Unnecessary
     * and the integer part of the result is not exact.
     */
    public ExtendedDecimal DivideToIntegerNaturalScale(
      ExtendedDecimal divisor, PrecisionContext ctx) {
      return math.DivideToIntegerNaturalScale(this, divisor, ctx);
    }

    /**
     * Divides this object by another object, and returns the integer part
     * of the result, with the exponent set to 0.
     * @param divisor The divisor.
     * @param ctx A precision context object to control the precision. The
     * rounding and exponent range settings of this context are ignored.
     * No flags will be set from this operation even if HasFlags of the context
     * is true. Can be null.
     * @return The integer part of the quotient of the two objects. The exponent
     * will be set to 0.
     * @throws ArithmeticException Attempted to divide by zero.
     * @throws ArithmeticException The result doesn't fit the given precision.
     */
    public ExtendedDecimal DivideToIntegerZeroScale(
      ExtendedDecimal divisor, PrecisionContext ctx) {
      return math.DivideToIntegerZeroScale(this, divisor, ctx);
    }
    
    /**
     * Finds the remainder that results when dividing two ExtendedDecimal
     * objects. The remainder is the value that remains when the absolute
     * value of this object is divided by the absolute value of the other object;
     * the remainder has the same sign (positive or negative) as this object.
     * @param divisor The divisor.
     * @param ctx A precision context object to control the precision. The
     * rounding and exponent range settings of this context are ignored.
     * No flags will be set from this operation even if HasFlags of the context
     * is true. Can be null.
     * @return The remainder of the two objects.
     * @throws ArithmeticException Attempted to divide by zero.
     * @throws ArithmeticException The result of integer division (the
     * quotient, not the remainder) wouldn't fit the given precision.
     */
    public ExtendedDecimal Remainder(
      ExtendedDecimal divisor, PrecisionContext ctx) {
      return math.Remainder(this, divisor, ctx);
    }
    /**
     * Finds the distance to the closest multiple of the given divisor, based
     * on the result of dividing this object's value by another object's
     * value. <ul> <li> If this and the other object divide evenly, the result
     * is 0.</li> <li>If the remainder's absolute value is less than half
     * of the divisor's absolute value, the result has the same sign as this
     * object and will be the distance to the closest multiple.</li> <li>If
     * the remainder's absolute value is more than half of the divisor's
     * absolute value, the result has the opposite sign of this object and
     * will be the distance to the closest multiple.</li> <li>If the remainder's
     * absolute value is exactly half of the divisor's absolute value, the
     * result has the opposite sign of this object if the quotient, rounded
     * down, is odd, and has the same sign as this object if the quotient, rounded
     * down, is even, and the result's absolute value is half of the divisor's
     * absolute value.</li> </ul> This function is also known as the "IEEE
     * Remainder" function.
     * @param divisor The divisor.
     * @param ctx A precision context object to control the precision. The
     * rounding and exponent range settings of this context are ignored
     * (the rounding mode is always ((treated instanceof HalfEven) ? (HalfEven)treated
     * : null)). No flags will be set from this operation even if HasFlags
     * of the context is true. Can be null.
     * @return The distance of the closest multiple.
     * @throws ArithmeticException Attempted to divide by zero.
     * @throws ArithmeticException Either the result of integer division
     * (the quotient) or the remainder wouldn't fit the given precision.
     */
    public ExtendedDecimal RemainderNear(
      ExtendedDecimal divisor, PrecisionContext ctx) {
      return math.RemainderNear(this, divisor, ctx);
    }

    /**
     * Finds the largest value that's smaller than the given value.
     * @param ctx A precision context object to control the precision and
     * exponent range of the result. The rounding mode from this context
     * is ignored. No flags will be set from this operation even if HasFlags
     * of the context is true.
     * @return Returns the largest value that's less than the given value.
     * Returns null if the result is negative infinity.
     * @throws java.lang.IllegalArgumentException "ctx" is null, the precision
     * is 0, or "ctx" has an unlimited exponent range.
     */
    public ExtendedDecimal NextMinus(
      PrecisionContext ctx
     ) {
      return math.NextMinus(this,ctx);
    }

    /**
     * Finds the smallest value that's greater than the given value.
     * @param ctx A precision context object to control the precision and
     * exponent range of the result. The rounding mode from this context
     * is ignored. No flags will be set from this operation even if HasFlags
     * of the context is true.
     * @return Returns the smallest value that's greater than the given
     * value. Returns null if the result is positive infinity.
     * @throws java.lang.IllegalArgumentException "ctx" is null, the precision
     * is 0, or "ctx" has an unlimited exponent range.
     */
    public ExtendedDecimal NextPlus(
      PrecisionContext ctx
     ) {
      return math.NextPlus(this,ctx);
    }
    
    /**
     * Finds the next value that is closer to the other object's value than
     * this object's value.
     * @param otherValue A ExtendedDecimal object.
     * @param ctx A precision context object to control the precision and
     * exponent range of the result. The rounding mode from this context
     * is ignored. No flags will be set from this operation even if HasFlags
     * of the context is true.
     * @return Returns the next value that is closer to the other object's
     * value than this object's value. Returns null if the result is infinity.
     * @throws java.lang.IllegalArgumentException "ctx" is null, the precision
     * is 0, or "ctx" has an unlimited exponent range.
     */
    public ExtendedDecimal NextToward(
      ExtendedDecimal otherValue,
      PrecisionContext ctx
     ) {
      return math.NextToward(this,otherValue,ctx);
    }

    
    /**
     * Divides this ExtendedDecimal object by another ExtendedDecimal
     * object. The preferred exponent for the result is this object's exponent
     * minus the divisor's exponent.
     * @param divisor The divisor.
     * @param ctx A precision context to control precision, rounding, and
     * exponent range of the result. If HasFlags of the context is true, will
     * also store the flags resulting from the operation (the flags are in
     * addition to the pre-existing flags). Can be null.
     * @return The quotient of the two objects. Returns null if the return
     * value would overflow the exponent range. A caller can handle a null
     * return value by treating it as positive infinity if both operands
     * have the same sign or as negative infinity if both operands have different
     * signs.
     * @throws ArithmeticException Attempted to divide by zero.
     * @throws ArithmeticException Either ctx is null or ctx's precision
     * is 0, and the result would have a nonterminating decimal expansion;
     * or, the rounding mode is Rounding.Unnecessary and the result is not
     * exact.
     */
    public ExtendedDecimal Divide(
      ExtendedDecimal divisor,
      PrecisionContext ctx
     ) {
      return math.Divide(this, divisor, ctx);
    }

    /**
     * Gets the greater value between two decimal fractions.
     * @param first A ExtendedDecimal object.
     * @param second A ExtendedDecimal object.
     * @param ctx A PrecisionContext object.
     * @return The larger value of the two objects.
     */
    public static ExtendedDecimal Max(
      ExtendedDecimal first, ExtendedDecimal second, PrecisionContext ctx) {
      return math.Max(first, second, ctx);
    }

    /**
     * Gets the lesser value between two decimal fractions.
     * @param first A ExtendedDecimal object.
     * @param second A ExtendedDecimal object.
     * @param ctx A PrecisionContext object.
     * @return The smaller value of the two objects.
     */
    public static ExtendedDecimal Min(
      ExtendedDecimal first, ExtendedDecimal second, PrecisionContext ctx) {
      return math.Min(first, second, ctx);
    }
    /**
     * Gets the greater value between two values, ignoring their signs.
     * If the absolute values are equal, has the same effect as Max.
     * @param first A ExtendedDecimal object.
     * @param second A ExtendedDecimal object.
     * @param ctx A PrecisionContext object.
     */
    public static ExtendedDecimal MaxMagnitude(
      ExtendedDecimal first, ExtendedDecimal second, PrecisionContext ctx) {
      return math.MaxMagnitude(first, second, ctx);
    }
    
    /**
     * Gets the lesser value between two values, ignoring their signs. If
     * the absolute values are equal, has the same effect as Min.
     * @param first A ExtendedDecimal object.
     * @param second A ExtendedDecimal object.
     * @param ctx A PrecisionContext object.
     */
    public static ExtendedDecimal MinMagnitude(
      ExtendedDecimal first, ExtendedDecimal second, PrecisionContext ctx) {
      return math.MinMagnitude(first, second, ctx);
    }
    
    /**
     * Gets the greater value between two decimal fractions.
     * @param first A ExtendedDecimal object.
     * @param second A ExtendedDecimal object.
     * @return The larger value of the two objects.
     */
    public static ExtendedDecimal Max(
      ExtendedDecimal first, ExtendedDecimal second) {
      return Max(first,second,null);
    }

    /**
     * Gets the lesser value between two decimal fractions.
     * @param first A ExtendedDecimal object.
     * @param second A ExtendedDecimal object.
     * @return The smaller value of the two objects.
     */
    public static ExtendedDecimal Min(
      ExtendedDecimal first, ExtendedDecimal second) {
      return Min(first,second,null);
    }
    /**
     * Gets the greater value between two values, ignoring their signs.
     * If the absolute values are equal, has the same effect as Max.
     * @param first A ExtendedDecimal object.
     * @param second A ExtendedDecimal object.
     */
    public static ExtendedDecimal MaxMagnitude(
      ExtendedDecimal first, ExtendedDecimal second) {
      return MaxMagnitude(first,second,null);
    }
    
    /**
     * Gets the lesser value between two values, ignoring their signs. If
     * the absolute values are equal, has the same effect as Min.
     * @param first A ExtendedDecimal object.
     * @param second A ExtendedDecimal object.
     */
    public static ExtendedDecimal MinMagnitude(
      ExtendedDecimal first, ExtendedDecimal second) {
      return MinMagnitude(first,second,null);
    }
    /**
     * Compares the mathematical values of this object and another object.
     * <p> This method is not consistent with the Equals method because two
     * different decimal fractions with the same mathematical value, but
     * different exponents, will compare as equal.</p>
     * @param other A ExtendedDecimal object.
     * @return Less than 0 if this object's value is less than the other value,
     * or greater than 0 if this object's value is greater than the other value
     * or if "other" is null, or 0 if both values are equal.
     */
    public int compareTo(
      ExtendedDecimal other) {
      return math.compareTo(this, other);
    }
    
    /**
     * 
     * @param other An ExtendedDecimal object.
     * @param ctx A PrecisionContext object.
     */
public ExtendedDecimal CompareToWithContext(
      ExtendedDecimal other, PrecisionContext ctx) {
      return math.CompareToWithContext(this, other, false, ctx);
    }
    
    /**
     * 
     * @param other An ExtendedDecimal object.
     * @param ctx A PrecisionContext object.
     */
public ExtendedDecimal CompareToSignal(
      ExtendedDecimal other, PrecisionContext ctx) {
      return math.CompareToWithContext(this, other, true, ctx);
    }

    /**
     * Finds the sum of this object and another object. The result's exponent
     * is set to the lower of the exponents of the two operands.
     * @param decfrac The number to add to.
     * @param ctx A precision context to control precision, rounding, and
     * exponent range of the result. If HasFlags of the context is true, will
     * also store the flags resulting from the operation (the flags are in
     * addition to the pre-existing flags). Can be null.
     * @return The sum of thisValue and the other object. Returns null if
     * the result would overflow the exponent range.
     */
    public ExtendedDecimal Add(
      ExtendedDecimal decfrac, PrecisionContext ctx) {
      return math.Add(this, decfrac, ctx);
    }

    /**
     * Returns a decimal fraction with the same value but a new exponent.
     * @param desiredExponent The desired exponent of the result.
     * @param ctx A precision context to control precision and rounding
     * of the result. If HasFlags of the context is true, will also store the
     * flags resulting from the operation (the flags are in addition to the
     * pre-existing flags). Can be null, in which case the default rounding
     * mode is HalfEven.
     * @return A decimal fraction with the same value as this object but with
     * the exponent changed.
     * @throws ArithmeticException An overflow error occurred, or the
     * result can't fit the given precision without rounding
     * @throws java.lang.IllegalArgumentException The exponent is outside of the
     * valid range of the precision context, if it defines an exponent range.
     */
    public ExtendedDecimal Quantize(
      BigInteger desiredExponent, PrecisionContext ctx) {
      return Quantize(new ExtendedDecimal(BigInteger.ONE,desiredExponent), ctx);
    }

    /**
     * Returns a decimal fraction with the same value but a new exponent.
     * @param ctx A precision context to control precision and rounding
     * of the result. If HasFlags of the context is true, will also store the
     * flags resulting from the operation (the flags are in addition to the
     * pre-existing flags). Can be null, in which case the default rounding
     * mode is HalfEven.
     * @param desiredExponentSmall A 32-bit signed integer.
     * @return A decimal fraction with the same value as this object but with
     * the exponent changed.
     * @throws ArithmeticException An overflow error occurred, or the
     * result can't fit the given precision without rounding
     * @throws java.lang.IllegalArgumentException The exponent is outside of the
     * valid range of the precision context, if it defines an exponent range.
     */
    public ExtendedDecimal Quantize(
      int desiredExponentSmall, PrecisionContext ctx) {
      return Quantize(new ExtendedDecimal(BigInteger.ONE,BigInteger.valueOf(desiredExponentSmall)), ctx);
    }

    /**
     * Returns a decimal fraction with the same value as this object but with
     * the same exponent as another decimal fraction.
     * @param otherValue A decimal fraction containing the desired exponent
     * of the result. The mantissa is ignored.
     * @param ctx A precision context to control precision and rounding
     * of the result. If HasFlags of the context is true, will also store the
     * flags resulting from the operation (the flags are in addition to the
     * pre-existing flags). Can be null, in which case the default rounding
     * mode is HalfEven.
     * @return A decimal fraction with the same value as this object but with
     * the exponent changed.
     * @throws ArithmeticException An overflow error occurred, or the
     * result can't fit the given precision without rounding.
     * @throws java.lang.IllegalArgumentException The new exponent is outside of
     * the valid range of the precision context, if it defines an exponent
     * range.
     */
    public ExtendedDecimal Quantize(
      ExtendedDecimal otherValue, PrecisionContext ctx) {
      return math.Quantize(this, otherValue, ctx);
    }
    /**
     * Returns a decimal fraction with the same value as this object but rounded
     * to an integer.
     * @param ctx A precision context to control precision and rounding
     * of the result. If HasFlags of the context is true, will also store the
     * flags resulting from the operation (the flags are in addition to the
     * pre-existing flags). Can be null, in which case the default rounding
     * mode is HalfEven.
     * @return A decimal fraction with the same value as this object but rounded
     * to an integer.
     * @throws ArithmeticException An overflow error occurred, or the
     * result can't fit the given precision without rounding.
     * @throws java.lang.IllegalArgumentException The new exponent must be changed
     * to 0 when rounding and 0 is outside of the valid range of the precision
     * context, if it defines an exponent range.
     */
    public ExtendedDecimal RoundToIntegralExact(
      PrecisionContext ctx) {
      return math.RoundToExponentExact(this, BigInteger.ZERO, ctx);
    }
    /**
     * Returns a decimal fraction with the same value as this object but rounded
     * to an integer, without adding the FlagInexact or FlagRounded flags.
     * @param ctx A precision context to control precision and rounding
     * of the result. If HasFlags of the context is true, will also store the
     * flags resulting from the operation (the flags are in addition to the
     * pre-existing flags), except that this function will never add the
     * FlagRounded and FlagInexact flags (the only difference between
     * this and RoundToExponentExact). Can be null, in which case the default
     * rounding mode is HalfEven.
     * @return A decimal fraction with the same value as this object but rounded
     * to an integer.
     * @throws ArithmeticException An overflow error occurred, or the
     * result can't fit the given precision without rounding.
     * @throws java.lang.IllegalArgumentException The new exponent must be changed
     * to 0 when rounding and 0 is outside of the valid range of the precision
     * context, if it defines an exponent range.
     */
    public ExtendedDecimal RoundToIntegralNoRoundedFlag(
      PrecisionContext ctx) {
      return math.RoundToExponentNoRoundedFlag(this, BigInteger.ZERO, ctx);
    }
    /**
     * Returns a decimal fraction with the same value as this object but rounded
     * to a given exponent.
     * @param exponent The minimum exponent the result can have. This is
     * the maximum number of fractional digits in the result, expressed
     * as a negative number. Can also be positive, which eliminates lower-order
     * places from the number. For example, -3 means round to the thousandth
     * (10^-3), and 3 means round to the thousand (10^3). A value of 0 rounds
     * the number to an integer.
     * @param ctx A precision context to control precision and rounding
     * of the result. If HasFlags of the context is true, will also store the
     * flags resulting from the operation (the flags are in addition to the
     * pre-existing flags). Can be null, in which case the default rounding
     * mode is HalfEven.
     * @return A decimal fraction rounded to the given exponent.
     * @throws ArithmeticException An overflow error occurred, or the
     * result can't fit the given precision without rounding.
     * @throws java.lang.IllegalArgumentException The new exponent must be changed
     * when rounding and the new exponent is outside of the valid range of
     * the precision context, if it defines an exponent range.
     */
    public ExtendedDecimal RoundToExponentExact(
      BigInteger exponent, PrecisionContext ctx) {
      return math.RoundToExponentExact(this, exponent, ctx);
    }
    /**
     * Returns a decimal fraction with the same value as this object but rounded
     * to a given exponent, without throwing an exception if the result overflows
     * or doesn't fit the precision range.
     * @param exponent The minimum exponent the result can have. This is
     * the maximum number of fractional digits in the result, expressed
     * as a negative number. Can also be positive, which eliminates lower-order
     * places from the number. For example, -3 means round to the thousandth
     * (10^-3, 0.0001), and 3 means round to the thousand (10^3, 1000). A
     * value of 0 rounds the number to an integer.
     * @param ctx A precision context to control precision, rounding, and
     * exponent range of the result. If HasFlags of the context is true, will
     * also store the flags resulting from the operation (the flags are in
     * addition to the pre-existing flags). Can be null, in which case the
     * default rounding mode is HalfEven.
     * @return A decimal fraction rounded to the closest value representable
     * in the given precision, meaning if the result can't fit the precision,
     * additional digits are discarded to make it fit. Returns null if the
     * result of the rounding would cause an overflow. The caller can handle
     * a null return value by treating it as positive or negative infinity
     * depending on the sign of this object's value.
     * @throws java.lang.IllegalArgumentException The new exponent must be changed
     * when rounding and the new exponent is outside of the valid range of
     * the precision context, if it defines an exponent range.
     */
    public ExtendedDecimal RoundToExponent(
      BigInteger exponent, PrecisionContext ctx) {
      return math.RoundToExponentSimple(this, exponent, ctx);
    }

    /**
     * Multiplies two decimal fractions. The resulting scale will be the
     * sum of the scales of the two decimal fractions.
     * @param op Another decimal fraction.
     * @param ctx A precision context to control precision, rounding, and
     * exponent range of the result. If HasFlags of the context is true, will
     * also store the flags resulting from the operation (the flags are in
     * addition to the pre-existing flags). Can be null.
     * @return The product of the two decimal fractions. If a precision context
     * is given, returns null if the result of rounding would cause an overflow.
     * The caller can handle a null return value by treating it as negative
     * infinity if this value and the other value have different signs, or
     * as positive infinity if this value and the other value have the same
     * sign.
     */
    public ExtendedDecimal Multiply(
      ExtendedDecimal op, PrecisionContext ctx) {
      return math.Multiply(this, op, ctx);
    }
    /**
     * Multiplies by one value, and then adds another value.
     * @param op The value to multiply.
     * @param augend The value to add.
     * @param ctx A precision context to control precision, rounding, and
     * exponent range of the result. If HasFlags of the context is true, will
     * also store the flags resulting from the operation (the flags are in
     * addition to the pre-existing flags). Can be null.
     * @return The result thisValue * multiplicand + augend. If a precision
     * context is given, returns null if the result of rounding would cause
     * an overflow. The caller can handle a null return value by treating
     * it as negative infinity if this value and the other value have different
     * signs, or as positive infinity if this value and the other value have
     * the same sign.
     */
    public ExtendedDecimal MultiplyAndAdd(
      ExtendedDecimal op, ExtendedDecimal augend, PrecisionContext ctx) {
      return math.MultiplyAndAdd(this, op, augend, ctx);
    }
    /**
     * 
     * @param op An ExtendedDecimal object.
     * @param augend An ExtendedDecimal object.
     * @param ctx A PrecisionContext object.
     */
public ExtendedDecimal MultiplyAndSubtract(
      ExtendedDecimal op, ExtendedDecimal augend, PrecisionContext ctx) {
      if((augend)==null)throw new NullPointerException("decfrac");
      ExtendedDecimal negated=((augend.flags&BigNumberFlags.FlagNaN)!=0) ? augend :
        ExtendedDecimal.CreateWithFlags(
          augend.mantissa,augend.exponent,
          augend.flags^BigNumberFlags.FlagNegative);
      return math.MultiplyAndAdd(this, op, negated, ctx);
    }

    /**
     * Rounds this object's value to a given precision, using the given rounding
     * mode and range of exponent.
     * @param ctx A context for controlling the precision, rounding mode,
     * and exponent range. Can be null.
     * @return The closest value to this object's value, rounded to the specified
     * precision. Returns the same value as this object if "context" is null
     * or the precision and exponent range are unlimited. Returns null if
     * the result of the rounding would cause an overflow. The caller can
     * handle a null return value by treating it as positive or negative infinity
     * depending on the sign of this object's value.
     */
    public ExtendedDecimal RoundToPrecision(
      PrecisionContext ctx) {
      return math.RoundToPrecision(this, ctx);
    }

    /**
     * 
     * @param ctx A PrecisionContext object.
     */
public ExtendedDecimal Plus(
      PrecisionContext ctx) {
      return math.Plus(this, ctx);
    }

    /**
     * Rounds this object's value to a given maximum bit length, using the
     * given rounding mode and range of exponent.
     * @param ctx A context for controlling the precision, rounding mode,
     * and exponent range. The precision is interpreted as the maximum bit
     * length of the mantissa. Can be null.
     * @return The closest value to this object's value, rounded to the specified
     * precision. Returns the same value as this object if "context" is null
     * or the precision and exponent range are unlimited. Returns null if
     * the result of the rounding would cause an overflow. The caller can
     * handle a null return value by treating it as positive or negative infinity
     * depending on the sign of this object's value.
     */
    public ExtendedDecimal RoundToBinaryPrecision(
      PrecisionContext ctx) {
      return math.RoundToBinaryPrecision(this, ctx);
    }

  }
