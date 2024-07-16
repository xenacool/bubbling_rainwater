package bubbling_rainwater.dataplane.wide_chain_ring;

public record HashPartition(Long partition) implements Comparable<HashPartition> {
    @Override
    public int compareTo(HashPartition o) {
        return partition.compareTo(o.partition);
    }
}
