package com.dobbinsoft.fw.core.annotation.param.transfer;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
public class PriceCenterTransfer implements CustomFieldTransfer<Integer, BigDecimal>{

    private static final BigDecimal HUNDRED = new BigDecimal("100");

    @Override
    public BigDecimal transfer(Integer integer) {
        if (integer == null) {
            return null;
        }
        return new BigDecimal(integer).divide(HUNDRED, 2, RoundingMode.HALF_UP);
    }

    @Override
    public Integer recover(BigDecimal bigDecimal) {
        if (bigDecimal == null) {
            return null;
        }
        return bigDecimal.multiply(HUNDRED).intValue();
    }

    @Override
    public Class<?> annotation() {
        return PriceCent.class;
    }

}
