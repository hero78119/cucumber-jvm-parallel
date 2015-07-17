package com.yahoo.jonaswu.cucumberparallel

import java.util.regex.Matcher
import java.util.regex.Pattern

/**
 * Created by jonaswu on 2015/3/9.
 */
class CucumberArgumentMapper {
    private static final monitorArgu = ['-f', '--plugin']
    private static final enum STATUS {
        READY,
        IGNORE
    }
    private STATUS status = STATUS.IGNORE

    public mapper(args, uniqueKey) {
        def res = []
        args.each { it ->
            if (status == STATUS.READY) {
                def _arg = it.replaceAll(~/([a-zA-Z0-9_]+\.json)/, uniqueKey + "-\$1")
                _arg = _arg.replaceAll(~/([a-zA-Z0-9_]+\.xml)/, uniqueKey + "-\$1")
                _arg = _arg.replaceAll(~/([a-zA-Z0-9_]+\.html)/, uniqueKey + "-\$1")
                println _arg
                res << _arg
                status = STATUS.IGNORE
            } else if (status == STATUS.IGNORE) {
                if (it in monitorArgu)
                    status = STATUS.READY
                res << it
            }
        }
        return res
    }

    public retriveJSONReportPath(args) {
        def res = ""
        args.each { it ->
            if (status == STATUS.READY) {
                Pattern pattern = Pattern.compile('json\\:(.*\\.json)')
                Matcher m = pattern.matcher(it)
                if (m.matches()) {
                    res = m.group(1)
                }
                status = STATUS.IGNORE
            } else if (status == STATUS.IGNORE) {
                if (it in monitorArgu)
                    status = STATUS.READY
                res << it
            }
        }
        return res
    }
}

