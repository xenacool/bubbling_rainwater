package bubbling_rainwater.dataplane.wide_chain_ring;

import java.util.*;

/**
 * Wide-Area Consistent Hashing Chains provide a topology of replication and partitioning for storage services. This
 * allows decoupling the logical plan from the physical plan in a highly consistent distributed database without the
 * need for a single-point-of-failure.
 *
 *
 */
public class WideChainRing {

    private final LogicalNode root;

    private final SortedMap<HashPartition, LogicalNode> logicalPartitionMap;

    private final SortedMap<LogicalNode, List<PhysicalNode>> physicalNodeMap;

    public WideChainRing(LogicalNode root) {
        this.root = root;
        this.logicalPartitionMap = new TreeMap<>();
        this.physicalNodeMap = new TreeMap<>();
    }

    public Map.Entry<HashPartition, LogicalNode> mapLogical(HashPartition element) {
        SequencedMap<HashPartition, LogicalNode> tailMap = this.logicalPartitionMap.tailMap(element);
        if (!tailMap.isEmpty()) {
            return tailMap.firstEntry();
        } else {
            assert !this.logicalPartitionMap.isEmpty();
            return this.logicalPartitionMap.firstEntry();
        }
    }

    public void putLogicalPartition(HashPartition hashPartition, LogicalNode logicalNode) {
        this.logicalPartitionMap.put(hashPartition, logicalNode);
    }

    public Map.Entry<LogicalNode, List<PhysicalNode>> mapPhysicalPartition(LogicalNode node) {
        SequencedMap<LogicalNode, List<PhysicalNode>> tailMap = this.physicalNodeMap.tailMap(node);
        if (!tailMap.isEmpty()) {
            return tailMap.firstEntry();
        } else {
            assert !this.physicalNodeMap.isEmpty();
            return this.physicalNodeMap.firstEntry();
        }
    }

    public void putPhysicalNode(LogicalNode node, PhysicalNode physicalNode) {
        List<PhysicalNode> physicalNodes = this.physicalNodeMap.getOrDefault(node, new ArrayList<>());
        physicalNodes.add(physicalNode);
        this.physicalNodeMap.put(node, physicalNodes);
    }
}
