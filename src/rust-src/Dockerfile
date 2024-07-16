FROM rust:1.80

WORKDIR /usr/src/myapp
COPY . .

RUN cargo build --release
CMD ["./target/release/bubbling_controlplane"]
