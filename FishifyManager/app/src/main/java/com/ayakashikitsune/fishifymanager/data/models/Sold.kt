package com.ayakashikitsune.fishifymanager.data.models

class Sold(var soldvalue : Int =0) {
    override fun toString(): String {
        if(soldvalue >= 1000){
            val stringsold = soldvalue.toString()
            val stringLength =  stringsold.length
            return buildString{
                append(when(stringLength){
                    4 -> "${stringsold[0]}.${stringsold[1]}k"
                    5 -> "${stringsold[0]}${stringsold[1]}.${stringsold[2]}k"
                    6 -> "${stringsold[0]}${stringsold[1]}${stringsold[2]}k"
                    7 -> "${stringsold[0]}.${stringsold[1]}m"
                    8 -> "${stringsold[0]}${stringsold[1]}.${stringsold[2]}m"
                    9 -> "${stringsold[0]}${stringsold[1]}${stringsold[2]}m"
                    10 -> "${stringsold[0]}.${stringsold[1]}b"
                    11 -> "${stringsold[0]}${stringsold[1]}.${stringsold[2]}b"
                    12 -> "${stringsold[0]}${stringsold[1]}${stringsold[2]}b"
                    else -> "0"
                })
            }
        }else{
            return soldvalue.toString()
        }
    }
}