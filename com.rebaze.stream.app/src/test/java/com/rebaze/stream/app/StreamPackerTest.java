package com.rebaze.stream.app;

import org.junit.Test;

import com.rebaze.distribution.DistributionBuilder;

public class StreamPackerTest
{
    @Test
    public void foo() {
        DistributionBuilder p = new StreamPacker();
        p.pack();
    }

}
