package bubbling_rainwater.dataplane.wide_chain_ring;

public record LogicalNode(Long id) implements Comparable<LogicalNode> {
    @Override
    public int compareTo(LogicalNode o) {
        return id.compareTo(o.id);
    }
}
