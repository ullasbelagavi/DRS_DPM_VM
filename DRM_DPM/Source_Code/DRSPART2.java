
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import com.vmware.vim25.Description;
import com.vmware.vim25.HostVMotionCompatibility;
import com.vmware.vim25.InvalidProperty;
import com.vmware.vim25.RuntimeFault;
import com.vmware.vim25.TaskInfo;
import com.vmware.vim25.VirtualDeviceConfigSpecFileOperation;
import com.vmware.vim25.VirtualDeviceConfigSpecOperation;
import com.vmware.vim25.VirtualDisk;
import com.vmware.vim25.VirtualDiskFlatVer2BackingInfo;
import com.vmware.vim25.VirtualEthernetCard;
import com.vmware.vim25.VirtualEthernetCardNetworkBackingInfo;
import com.vmware.vim25.VirtualLsiLogicController;
import com.vmware.vim25.VirtualMachineConfigSpec;
import com.vmware.vim25.VirtualMachineFileInfo;
import com.vmware.vim25.VirtualMachineMovePriority;
import com.vmware.vim25.VirtualMachinePowerState;
import com.vmware.vim25.VirtualPCNet32;
import com.vmware.vim25.VirtualSCSISharing;
import com.vmware.vim25.mo.ComputeResource;
import com.vmware.vim25.mo.Datacenter;
import com.vmware.vim25.mo.Folder;
import com.vmware.vim25.mo.HostSystem;
import com.vmware.vim25.mo.InventoryNavigator;
import com.vmware.vim25.mo.ManagedEntity;
import com.vmware.vim25.mo.ResourcePool;
import com.vmware.vim25.mo.ServiceInstance;
import com.vmware.vim25.mo.Task;
import com.vmware.vim25.mo.VirtualMachine;

import java.rmi.RemoteException;


public class DRSPART2 {
	public static String vHostName;
	static final String SERVER_NAME = "130.65.132.244";
	static final String vHost_userName = "root";
	static final String USER_NAME = "administrator"; // root
	static final String PASSWORD = "12!@qwQW";
	
	static String url = "https://" + SERVER_NAME + "/sdk";
	
	
	private ServiceInstance si;
	private CommonConstants cm;
	private static void migrateVM(ManagedEntity vmc, ManagedEntity target) {
		ComputeResource cr = (ComputeResource) target.getParent();
		VirtualMachine vm=(VirtualMachine)vmc;
		try {
			Task task=null;
			System.out.println(vm.getRuntime().getPowerState().toString());
			if(vm.getRuntime().getPowerState().toString()=="poweredOn")
				task = vm.migrateVM_Task(cr.getResourcePool(), (HostSystem) target,
				        VirtualMachineMovePriority.highPriority, 
				        VirtualMachinePowerState.poweredOn);
			else
				task = vm.migrateVM_Task(cr.getResourcePool(), (HostSystem) target,
				        VirtualMachineMovePriority.highPriority, 
				        VirtualMachinePowerState.poweredOff);
			if(task.waitForTask()==Task.SUCCESS){
				System.out.println("VM Migrated!");
				
			}
			else{
				System.out.println("VM Migration failed!");
			      TaskInfo info = task.getTaskInfo();
			      System.out.println(info.getError().getFault());
			}
			
		} catch ( RemoteException | InterruptedException e) {
			e.printStackTrace();
		}
		
	}
	
	public static void main(String args[]) {
		
			Thread t1 = new Thread() {
				
				public void run() {	
			
			HostSystem less = null;
			HostSystem highest = null;
			
			
			ServiceInstance si;
			try {
				si = new ServiceInstance(new URL(url), USER_NAME,
				        PASSWORD, true);
				Folder root=si.getRootFolder();
				ManagedEntity[] hosts;
				hosts = new InventoryNavigator(root).searchManagedEntities("HostSystem");
			
			
			double first = CPUUsage.getHostCPUUsage("130.65.132.245");
			double last = CPUUsage.getHostCPUUsage("130.65.132.245");
			for(int i=0; i< hosts.length; i++){
			    if(CPUUsage.getHostCPUUsage(hosts[i].getName())<=first){
			    	first=CPUUsage.getHostCPUUsage(hosts[i].getName());
			    	less=(HostSystem) hosts[i];
			    	
			    }
			    if(CPUUsage.getHostCPUUsage(hosts[i].getName())>=last){
			    	last=CPUUsage.getHostCPUUsage(hosts[i].getName());
			    	highest=(HostSystem) hosts[i];	
			    }
			}
			
			int j=1;
			double avgVM=0.0;
			double total=0.0;
			int number=0;
			
				for (VirtualMachine v : highest.getVms())
				{
					System.out.println(v.getName());
					if (v.getRuntime().getPowerState().toString()=="poweredOn")
					{
					
					//System.out.println(v.getName());
					total=total+CPUUsage.getVMCPUUsage(v.getName());
					number++;
				}
				}
			
			
			//int no=highest.getVms().length;
			System.out.println("Number of Machines running = : " + number);
			avgVM=total/number;
			System.out.println("Average of vms: "+avgVM);
			
				for (VirtualMachine v : highest.getVms())
					
				{	
					if (v.getRuntime().getPowerState().toString()=="poweredOn")
						{
					System.out.println(v.getName()+":"+CPUUsage.getVMCPUUsage(v.getName()));
					if(CPUUsage.getVMCPUUsage(v.getName())<avgVM){
						System.out.println(v.getName()+":"+CPUUsage.getVMCPUUsage(v.getName()));
						//if (v.getRuntime().getPowerState().toString()=="powerOn"
						migrateVM(v, less);
					}
				}
}
				Thread.sleep(3000000);
			}
			catch(Exception e){
				e.printStackTrace();
			}
 
		}

		};
		t1.start();
		
	}
}
	

	
