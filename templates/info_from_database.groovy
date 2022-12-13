#!/usr/bin/env groovy

import groovy.sql.Sql


def output = []
def sql = Sql.newInstance('jdbc:mysql://HOST:PORT/DATABASE', 'user', 'password', 'com.mysql.jdbc.Driver')
def sqlString='select game_name from game where order by game_name;'
sql.eachRow(sqlString){
    output.push(it[0])
}
sql.close()
return output

// --------------------------------------------------------------------------

import groovy.sql.Sql 

def output = []
def operator_name = []

if (ENVIRONMENT.equals("pre-prod")) {
    def sql = Sql.newInstance('jdbc:mysql://HOST:PORT/DATABASE', 'user', 'password', 'com.mysql.jdbc.Driver')

    def sqlId="select game_id from core_game where game_name='$GAME_NAME'"

    sql.eachRow(sqlId) {
        output.push(it[0])
    }

    output.each {
        def sqlOperator = "SELECT DISTINCT core_operator.operator_name FROM core_live_game LEFT JOIN core_operator ON core_live_game.operator_id = core_operator.operator_id WHERE core_live_game.game_id=${it};"
        sql.eachRow(sqlOperator) {
            operator_name.push(it[0])
        }
    }

    sql.close()

    return operator_name
}

// --------------------------------------------------------------------------

import groovy.sql.Sql

def output = []

def sql=Sql.newInstance("jdbc:mysql://HOST:PORT/DATABASE", "user", "password", "com.mysql.jdbc.Driver")
def sqlString="select distinct(operator_name) from mv_finance_report where payment_date>='$START_PAYMENT_DATE' and payment_date<'$FINISH_PAYMENT_DATE' and operator_name like '%GVC%';"

sql.eachRow(sqlString) {
    output.push(it[0])
    }  
    sql.close()
    return output
