package com.mycelium.bequant.market.model

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.mycelium.wallet.Utils
import com.mycelium.wapi.wallet.coins.Value


class ExchangeViewModel : ViewModel() {
    val available = MutableLiveData<Value>(Value.valueOf(Utils.getBtcCoinType(), 1212312312))
    val youSend = MutableLiveData<Value>(Value.valueOf(Utils.getBtcCoinType(), 1212312312))
    val youGet = MutableLiveData<Value>(Value.valueOf(Utils.getBtcCoinType(), 1212312312))
}