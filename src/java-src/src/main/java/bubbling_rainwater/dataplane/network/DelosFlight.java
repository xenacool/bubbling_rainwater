package bubbling_rainwater.dataplane.network;

import org.apache.arrow.flight.*;

public class DelosFlight implements FlightProducer {
    public DelosFlight() {

    }

    @Override
    public void getStream(CallContext callContext, Ticket ticket, ServerStreamListener serverStreamListener) {

    }

    @Override
    public void listFlights(CallContext callContext, Criteria criteria, StreamListener<FlightInfo> streamListener) {

    }

    @Override
    public FlightInfo getFlightInfo(CallContext callContext, FlightDescriptor flightDescriptor) {
        return null;
    }

    @Override
    public Runnable acceptPut(CallContext callContext, FlightStream flightStream, StreamListener<PutResult> streamListener) {
        return null;
    }

    @Override
    public void doAction(CallContext callContext, Action action, StreamListener<Result> streamListener) {

    }

    @Override
    public void listActions(CallContext callContext, StreamListener<ActionType> streamListener) {

    }
}
