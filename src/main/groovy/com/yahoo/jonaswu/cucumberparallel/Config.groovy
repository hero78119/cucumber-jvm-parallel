package com.yahoo.jonaswu.cucumberparallel

import java.lang.reflect.Field
import java.lang.reflect.Modifier

/**
 * Created by jonaswu on 2015/3/10.
 */
class Config {

    static int max_instance = 4
    static int isdebug = 0

    static validator = [
            'max_instance': { val -> Integer.valueOf(val) },
            'isdebug'     : { val -> Integer.valueOf(val) }
    ]

    private static final enum STATUS {
        READY,
        IGNORE
    }

    static parseAndFilter(args) {
        def res = []
        def status = STATUS.IGNORE
        def targetArguName = ''
        args.each { it ->
            if (status == STATUS.READY) {
                println it
                Config."$targetArguName" = validator[targetArguName](it)
                status = STATUS.IGNORE
            } else if (status == STATUS.IGNORE) {
                def arguName = it.replace('-', '')
                try {
                    if (Config."$arguName") {
                        status = STATUS.READY
                        targetArguName = arguName
                    }
                } catch (MissingPropertyException e) {
                    res << it //variable belong to cucumber
                }

            }
        }
        return res
    }

}
