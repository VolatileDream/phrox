namespace java orb.quantum.phrox.internal.thrift

struct Authorization {
	1: string method,
	2: binary data
}

struct PhroxLocation {
	1: string host,
	2: i32 port = 8080
}

exception NotAuthorized {
	1: string reason
}

service PhroxConnectionHandler {

	PhroxLocation connect( 1: optional Authorization auth, 2: PhroxLocation location ) throws (1: NotAuthorized nope)
}
