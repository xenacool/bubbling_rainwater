package bubbling_rainwater.dataplane.wide_chain_ring;

import org.junit.Assert;
import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class WideChainRingTest {
    @Test
    public void testWideHashingChainLogicalMapping() {
        LogicalNode zeroNode = new LogicalNode(0L);
        WideChainRing ring = new WideChainRing(zeroNode);
        HashPartition zeroPartition = new HashPartition(0L);
        ring.putLogicalPartition(zeroPartition, zeroNode);
        LogicalNode halfNode = new LogicalNode(1L);
        HashPartition halfPartition = new HashPartition(Long.MAX_VALUE / 2L);
        ring.putLogicalPartition(halfPartition, halfNode);
        Assert.assertEquals(ring.mapLogical(new HashPartition(Long.MAX_VALUE / 4)), Map.entry(halfPartition, halfNode));
        Assert.assertEquals(ring.mapLogical(new HashPartition((Long.MAX_VALUE / 4)*3)), Map.entry(zeroPartition, zeroNode));
    }

    @Test
    public void testWideHashingChainPhysicalMapping() {
        WideChainRing ring = new WideChainRing(new LogicalNode(0L));
        PhysicalNode e1 = new PhysicalNode(UUID.nameUUIDFromBytes("zero".getBytes(StandardCharsets.UTF_8)));
        PhysicalNode e2 = new PhysicalNode(UUID.nameUUIDFromBytes("one".getBytes(StandardCharsets.UTF_8)));
        ring.putPhysicalNode(new LogicalNode(0L), e1);
        ring.putPhysicalNode(new LogicalNode(1L), e2);
        Assert.assertEquals(ring.mapPhysicalPartition(new LogicalNode(0L)), Map.entry(new LogicalNode(0L), List.of(e1)));
        Assert.assertEquals(ring.mapPhysicalPartition(new LogicalNode(1L)), Map.entry(new LogicalNode(1L), List.of(e2)));

    }
}
