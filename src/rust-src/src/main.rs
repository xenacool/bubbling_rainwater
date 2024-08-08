use std::env::current_dir;
use std::sync::Arc;

use datafusion::execution::options::{
    ArrowReadOptions, AvroReadOptions, CsvReadOptions, NdJsonReadOptions, ParquetReadOptions,
};
use datafusion::prelude::SessionContext;
// use pgwire::api::auth::noop::NoopStartupHandler;
// use pgwire::api::copy::NoopCopyHandler;
// use pgwire::api::PgWireHandlerFactory;
// use pgwire::tokio::process_socket;
use structopt::StructOpt;
use tokio::net::TcpListener;

// mod datatypes;
// mod handlers;

#[derive(Debug, StructOpt)]
#[structopt(
    name = "datafusion-postgres",
    about = "A postgres interface for datatfusion. Serve any CSV/JSON/Arrow files as tables."
)]
struct Opt {
    /// CSV files to register as table, using syntax `table_name:file_path`
    #[structopt(long("csv"))]
    csv_tables: Vec<String>,
    /// JSON files to register as table, using syntax `table_name:file_path`
    #[structopt(long("json"))]
    json_tables: Vec<String>,
    /// Arrow files to register as table, using syntax `table_name:file_path`
    #[structopt(long("arrow"))]
    arrow_tables: Vec<String>,
    /// Parquet files to register as table, using syntax `table_name:file_path`
    #[structopt(long("parquet"))]
    parquet_tables: Vec<String>,
    /// Avro files to register as table, using syntax `table_name:file_path`
    #[structopt(long("avro"))]
    avro_tables: Vec<String>,
}

fn parse_table_def(table_def: &str) -> (&str, &str) {
    table_def
        .split_once(':')
        .expect("Use this pattern to register table: table_name:file_path")
}

// struct HandlerFactory(Arc<handlers::DfSessionService>);

// impl PgWireHandlerFactory for HandlerFactory {
//     type StartupHandler = NoopStartupHandler;
//     type SimpleQueryHandler = handlers::DfSessionService;
//     type ExtendedQueryHandler = handlers::DfSessionService;
//     type CopyHandler = NoopCopyHandler;
//
//     fn simple_query_handler(&self) -> Arc<Self::SimpleQueryHandler> {
//         self.0.clone()
//     }
//
//     fn extended_query_handler(&self) -> Arc<Self::ExtendedQueryHandler> {
//         self.0.clone()
//     }
//
//     fn startup_handler(&self) -> Arc<Self::StartupHandler> {
//         Arc::new(NoopStartupHandler)
//     }
//
//     fn copy_handler(&self) -> Arc<Self::CopyHandler> {
//         Arc::new(NoopCopyHandler)
//     }
// }
//
#[tokio::main]
async fn main() {
    println!("Current working directory is {:?}", current_dir());
    let opts = Opt::from_args();

    let session_context = SessionContext::new();

    // let factory = Arc::new(HandlerFactory(Arc::new(handlers::DfSessionService::new(
    //     session_context,
    // ))));

    let server_addr = "127.0.0.1:5432";
    let listener = TcpListener::bind(server_addr).await.unwrap();
    println!("Listening to {}", server_addr);
    loop {
        let incoming_socket = listener.accept().await.unwrap();
        // let factory_ref = factory.clone();

        // tokio::spawn(async move { process_socket(incoming_socket.0, None, factory_ref).await });
    }
}
