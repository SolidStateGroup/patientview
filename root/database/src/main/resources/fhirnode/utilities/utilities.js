exports.make_error = function(err, msg) {
	var e = new Error(msg);
	e.code = err;
	return e;
}

exports.send_success = function(res, data) {
	res.writeHead(200, {"Content-Type": "application/json"});
	res.end(JSON.stringify(data) + "\n");
}

exports.send_failure = function(res, code, err) {
	var code = (err.code) ? err.code : err.name;
	res.writeHead(code, {"Content-Type": "application/json"});
	res.end(JSON.stringify({error: code, message : err.message}) + "\n");
}

exports.invalid_resource = function () {
	return make_error("invalid_resource", "the requested resource does not exist.");
}

exports.no_such_patient = function () {
	return make_error("no_such_patient", "the specific patient does not exist.")
}
