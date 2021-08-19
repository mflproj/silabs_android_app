/*
 * Copyright © 2019 Silicon Labs, http://www.silabs.com. All rights reserved.
 */

package com.siliconlabs.bluetoothmesh.App.Fragments.NetworkList


import android.util.Log
import com.siliconlab.bluetoothmesh.adk.ErrorType
import com.siliconlab.bluetoothmesh.adk.data_model.subnet.Subnet
import com.siliconlab.bluetoothmesh.adk.data_model.subnet.SubnetChangeNameException
import com.siliconlab.bluetoothmesh.adk.data_model.subnet.SubnetCreationException
import com.siliconlabs.bluetoothmesh.App.BasePresenter
import com.siliconlabs.bluetoothmesh.App.Logic.MeshLogic
import com.siliconlabs.bluetoothmesh.App.Logic.NetworkConnectionListener
import com.siliconlabs.bluetoothmesh.App.Logic.NetworkConnectionLogic
import com.siliconlabs.bluetoothmesh.App.Models.MeshNetworkManager
import com.siliconlabs.bluetoothmesh.App.Models.MeshNodeManager

class NetworkListPresenter(private val networkListView: NetworkListView, private val meshLogic: MeshLogic, private val meshNetworkManager: MeshNetworkManager, val networkConnectionLogic: NetworkConnectionLogic, val meshNodeManager: MeshNodeManager) : BasePresenter {
    private val TAG: String = javaClass.canonicalName!!

    private val network = meshLogic.currentNetwork!!

    override fun onResume() {
        refreshList()
    }

    override fun onPause() {
    }

    private fun refreshList() {
        networkListView.setNetworkList(network.subnets)
    }

    // View callbacks

    fun addNetwork(name: String): Boolean {
        if (name.trim().isEmpty()) {
            return false
        }

        try {
            meshNetworkManager.createSubnet(name)
        } catch (e: SubnetCreationException) {
            Log.e(TAG, e.toString())
            networkListView.showToast(NetworkListView.TOAST_MESSAGE.ERROR_CREATING_NETWORK)
        }

        refreshList()
        return true
    }

    fun updateNetwork(networkInfo: Subnet, newName: String): Boolean {
        if (newName.trim().isEmpty()) {
            return false
        }

        try {
            networkInfo.name = newName
            networkListView.showToast(NetworkListView.TOAST_MESSAGE.SUCCESS_UPDATE)
        } catch (e: SubnetChangeNameException) {
            return false
        }

        refreshList()
        return true
    }

    fun deleteNetwork(networkInfo: Subnet) {
        networkListView.showLoadingDialog()
        if (networkInfo.nodes.isEmpty()) {
            removeNetwork(networkInfo)
        } else {
            removeNetworkWithNodes(networkInfo)
        }
    }

    fun deleteNetworkLocally(subnet: Subnet) {
        subnet.removeOnlyFromLocalStructure()
        refreshList()
    }

    private fun removeNetwork(subnet: Subnet) {
        meshNetworkManager.removeSubnet(subnet, object : MeshNetworkManager.DeleteNetworksCallback {
            override fun success() {
                Log.d(TAG, "removeSubnet success")
                networkListView.dismissLoadingDialog()
                refreshList()
            }

            override fun error(subnet: Subnet?, error: ErrorType?) {
                Log.d(TAG, "removeSubnet error")
                networkListView.dismissLoadingDialog()
                networkListView.showDeleteNetworkLocallyDialog(subnet!!, error!!)
                refreshList()
            }
        })
    }

    private fun removeNetworkWithNodes(subnet: Subnet) {
        networkConnectionLogic.addListener(object : NetworkConnectionListener {
            fun clear() {
                refreshList()
                networkConnectionLogic.disconnect()
                networkConnectionLogic.removeListener(this)
            }

            override fun connecting() {
                Log.d(TAG, "connecting")
                networkListView.updateLoadingDialogMessage(NetworkListView.LOADING_DIALOG_MESSAGE.CONNECTING_TO_NETWORK, subnet.name)
            }

            override fun connected() {
                Log.d(TAG, "connected")
                networkListView.updateLoadingDialogMessage(NetworkListView.LOADING_DIALOG_MESSAGE.REMOVING_NETWORK, subnet.name)
                meshNetworkManager.removeSubnet(subnet, object : MeshNetworkManager.DeleteNetworksCallback {
                    override fun success() {
                        Log.d(TAG, "removeSubnet success")
                        networkConnectionLogic.disconnect()
                        removeNodesFunc(subnet)
                        networkListView.dismissLoadingDialog()
                        refreshList()
                    }

                    override fun error(subnet: Subnet?, error: ErrorType?) {
                        Log.d(TAG, "removeSubnet error")
                        networkListView.dismissLoadingDialog()
                        networkListView.showDeleteNetworkLocallyDialog(subnet!!, error!!)
                        clear()
                    }
                })
                networkConnectionLogic.removeListener(this)
            }

            override fun disconnected() {
                Log.d(TAG, "disconnected")
            }

            override fun initialConfigurationLoaded() {
            }

            override fun connectionMessage(messageType: NetworkConnectionListener.MessageType) {
                Log.d(TAG, "connectionMessage")
                clear()
                networkListView.updateLoadingDialogMessage(NetworkListView.LOADING_DIALOG_MESSAGE.CONNECTING_TO_NETWORK_ERROR, messageType.toString(), true)
            }

            override fun connectionErrorMessage(error: ErrorType) {
                Log.d(TAG, "connectionErrorMessage")
                clear()
                networkListView.dismissLoadingDialog()
                networkListView.showDeleteNetworkLocallyDialog(subnet, error)
            }
        })
        networkConnectionLogic.connect(subnet)
    }

    fun removeNodesFunc(subnet: Subnet) {
        meshNodeManager.getMeshNodes(subnet).forEach {
            meshNodeManager.removeNodeFunc(it)
        }
    }

    fun networkClicked(subnet: Subnet) {
        meshLogic.currentSubnet = subnet
        networkListView.showNetworkFragment()
    }

}
