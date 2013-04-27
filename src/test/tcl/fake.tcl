#!/usr/bin/tclsh
socket -server a 10000
proc a {c addr port} {
	puts $c "HTTP/1.0 200 OK"
	puts $c "Date: [clock format [clock seconds]]"
	puts $c "Content-Type: text/plain"
	puts $c ""
	puts $c "fake"
	flush $c
	after 100
	close $c
	puts "done"
}

puts "listening on localhost:10000"

vwait forever
