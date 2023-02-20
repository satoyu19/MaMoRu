package jp.ac.jec.cm0119.mamoru.utils.uistate

data class BeaconState(
        var isSuccess: Boolean? = null,
        var beaconId: String? = null,
        var errorMessage: String? = null
)