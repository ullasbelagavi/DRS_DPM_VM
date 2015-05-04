package com.vmware.vim25.mo.samples;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;




import CONFIG.SJSULabConfig;

import com.oracle.webservices.internal.literal.ArrayList;
import com.vmware.vim25.mo.Folder;
import com.vmware.vim25.mo.InventoryNavigator;
import com.vmware.vim25.mo.ManagedEntity;
import com.vmware.vim25.mo.ServiceInstance;
import com.vmware.vim25.mo.VirtualMachine;

public class CollectVMStatistics {
	//VMInstance[] myvms;
	ServiceInstance si = null;
	public void pingvms() throws IOException{
		URL url = new URL(SJSULabConfig.getvCenterURL());
		si = new ServiceInstance(url, SJSULabConfig.getvCenterUsername(), SJSULabConfig.getPassword(), true);
		Folder rootFolder = si.getRootFolder();
		String rootname = rootFolder.getName();
		//System.out.println("\tRoot name for vCenter: " + rootname + "");
		ManagedEntity[] mes = new InventoryNavigator(rootFolder).searchManagedEntities("VirtualMachine");
		if (mes == null || mes.length == 0) {
			System.out.println("\n\tNo Virtual machine Found !!!");
			return;
		}
		java.util.ArrayList<VMStats> list = new java.util.ArrayList<VMStats>();
		for (int i = 0; i < mes.length; i++) {
			VirtualMachine vm = (VirtualMachine) mes[i];
			System.out.println("Collecting stats for vm : "+vm.getName());
			Runnable task = new VMStats(vm.getName());
			System.out.println(vm.getName());
			Thread worker = new Thread(task);
			worker.start();
			//VMHealthUpdateThread vmobj = new VMHealthUpdateThread(vm.getName());
			//list.add(vmobj);
		}
		for (VMStats vmHealthUpdateThread : list) {
			
			vmHealthUpdateThread.run();
		}
	}
	public static void main(String[] args) throws Exception {
		CollectVMStatistics obj = new CollectVMStatistics();
		obj.pingvms();
	}
}


