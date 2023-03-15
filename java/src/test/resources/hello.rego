package play

# Customers should be able to view their own payments
default allow = false
allow = true {
    input.method = "GET"
}
