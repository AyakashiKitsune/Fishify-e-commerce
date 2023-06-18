package com.ayakashikitsune.fishifymanager.data.models

data class AnnoucementData(
    val title : String,
    val description : String,
    val imagebg : String,
    val action: String = AnnoucementAction.GOTO.state,
    val afterAction : String
)

enum class AnnoucementAction(val state: String){
    GOTO("GOTO"),
    POPOUT("POPOUT")
}