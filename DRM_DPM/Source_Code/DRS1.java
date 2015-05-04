

import java.net.URL;

import com.vmware.vim25.Description;
import com.vmware.vim25.VirtualDeviceConfigSpec;
import com.vmware.vim25.VirtualDeviceConfigSpecFileOperation;
import com.vmware.vim25.VirtualDeviceConfigSpecOperation;
import com.vmware.vim25.VirtualDisk;
import com.vmware.vim25.VirtualDiskFlatVer2BackingInfo;
import com.vmware.vim25.VirtualEthernetCard;
import com.vmware.vim25.VirtualEthernetCardNetworkBackingInfo;
import com.vmware.vim25.VirtualLsiLogicController;
import com.vmware.vim25.VirtualMachineConfigSpec;
import com.vmware.vim25.VirtualMachineFileInfo;
import com.vmware.vim25.VirtualPCNet32;
import com.vmware.vim25.VirtualSCSISharing;
import com.vmware.vim25.mo.Datacenter;
import com.vmware.vim25.mo.Folder;
import com.vmware.vim25.mo.HostSystem;
import com.vmware.vim25.mo.InventoryNavigator;
import com.vmware.vim25.mo.ResourcePool;
import com.vmware.vim25.mo.ServiceInstance;
import com.vmware.vim25.mo.Task;
import com.vmware.vim25.mo.VirtualMachine;

public class DRS1 
{
	//Create VM
	static final String SERVER_NAME = "130.65.132.244";
	static final String USER_NAME = "administrator"; //root
	static final String PASSWORD = "12!@qwQW";
	static String url = "https://" + SERVER_NAME + "/sdk";
    static String dcName = "DC_T12"; //ha-datacenter
    static String vmName = "Vnnew1";
    static long memorySizeMB = 512;
    static int cupCount = 1;
    static String guestOsId = "sles10Guest";
    static long diskSizeKB = 10000;
    static String diskMode = "persistent";
    static String datastoreName = "nfs2team12"; 
    static String netName = "VM Network";
    static String nicName = "Network Adapter 1";
    
   
	
	//private static HashMap<String, Double> h1 = new HashMap<String, Double>();
    
	public static void main(String args[])
	{
		try
		{
			
			double first = CPUUsage.getHostCPUUsage("130.65.132.245");
			
			System.out.println("CPU usage of first host:" +first);
			
			
			double second = CPUUsage.getHostCPUUsage("130.65.132.246");
			
			System.out.println("CPU usage of second host:"+second);
			
			ServiceInstance si = new ServiceInstance(new URL(url),
					USER_NAME, PASSWORD, true);
		
		    Folder rootFolder = si.getRootFolder();
		    HostSystem host=null;
		    
		    Datacenter dc = (Datacenter) new InventoryNavigator(rootFolder).searchManagedEntity("Datacenter", dcName);

		    ResourcePool rp;
			if(first < second)
			{
			     host= (HostSystem) new InventoryNavigator(rootFolder).searchManagedEntity("HostSystem" , "130.65.132.245");
			    		//+ "//change number here for which vHost to use [0 or 1]
			    rp = (ResourcePool) new InventoryNavigator(dc).searchManagedEntities("ResourcePool")[0]; 
			}
			else
			{
				 host= (HostSystem) new InventoryNavigator(rootFolder).searchManagedEntity("HostSystem" , "130.65.132.246");
				//change number here for which vHost to use [0 or 1]
			    rp = (ResourcePool) new InventoryNavigator(dc).searchManagedEntities("ResourcePool")[1]; 
			}
			
		    Folder vmFolder = dc.getVmFolder();
		
		    // create vm config spec
		    VirtualMachineConfigSpec vmSpec = new VirtualMachineConfigSpec();
		    
		       
		    vmSpec.setName(vmName);
		    vmSpec.setAnnotation("VirtualMachine Annotation");
		    vmSpec.setMemoryMB(memorySizeMB);
		    vmSpec.setNumCPUs(cupCount);
		    vmSpec.setGuestId(guestOsId);
		
		    // create virtual devices
		    int cKey = 1000;
		    VirtualDeviceConfigSpec scsiSpec = createScsiSpec(cKey);
		    VirtualDeviceConfigSpec diskSpec = createDiskSpec(datastoreName, cKey, diskSizeKB, diskMode);
		    VirtualDeviceConfigSpec nicSpec = createNicSpec(netName, nicName);
		
		    vmSpec.setDeviceChange(new VirtualDeviceConfigSpec[]{scsiSpec, diskSpec, nicSpec});
		    
		    // create vm file info for the vmx file
		    VirtualMachineFileInfo vmfi = new VirtualMachineFileInfo();
		    vmfi.setVmPathName("["+ datastoreName +"]");
		    vmSpec.setFiles(vmfi);
		
		    // call the createVM_Task method on the vm folder
		    Task task = vmFolder.createVM_Task(vmSpec, rp, null);
		   @SuppressWarnings("deprecation")
		String result = task.waitForMe();
		    System.out.println("VM creation...");
		    
		    if(result == Task.SUCCESS) 
		    {
		      System.out.println("VM Created Sucessfully");
		      VirtualMachine vmo= (VirtualMachine) new InventoryNavigator(rootFolder).searchManagedEntity("VirtualMachine", vmName);
		      Task task2 = vmo.powerOnVM_Task((HostSystem)host);
		      @SuppressWarnings("deprecation")
		      String tsk=task2.waitForMe();
				if (tsk == Task.SUCCESS) {
					System.out.println("New VM Powered ON!! ");
				}
				else
					System.out.println("VM cannot be powered on");
		    }
		    else 
		    {
		      System.out.println("VM could not be created. ");
		    }
		}
		catch(Exception e)
		{
			System.out.println("Exception caught");
			e.printStackTrace();
		}
  }

	
	
	static VirtualDeviceConfigSpec createScsiSpec(int cKey)
	{
		VirtualDeviceConfigSpec scsiSpec = new VirtualDeviceConfigSpec();
		scsiSpec.setOperation(VirtualDeviceConfigSpecOperation.add);
		VirtualLsiLogicController scsiCtrl = new VirtualLsiLogicController();
		scsiCtrl.setKey(cKey);
		scsiCtrl.setBusNumber(0);
		scsiCtrl.setSharedBus(VirtualSCSISharing.noSharing);
		scsiSpec.setDevice(scsiCtrl);
		return scsiSpec;
	}
  
	static VirtualDeviceConfigSpec createDiskSpec(String dsName, int cKey, long diskSizeKB, String diskMode)
	{
		VirtualDeviceConfigSpec diskSpec = new VirtualDeviceConfigSpec();
		diskSpec.setOperation(VirtualDeviceConfigSpecOperation.add);
		diskSpec.setFileOperation(VirtualDeviceConfigSpecFileOperation.create);
		
		VirtualDisk vd = new VirtualDisk();
		vd.setCapacityInKB(diskSizeKB);
		diskSpec.setDevice(vd);
		vd.setKey(0);
		vd.setUnitNumber(0);
		vd.setControllerKey(cKey);
		
		VirtualDiskFlatVer2BackingInfo diskfileBacking = new VirtualDiskFlatVer2BackingInfo();
		String fileName = "["+ dsName +"]";
		diskfileBacking.setFileName(fileName);
		diskfileBacking.setDiskMode(diskMode);
		diskfileBacking.setThinProvisioned(true);
		vd.setBacking(diskfileBacking);
		return diskSpec;
	}
  
	static VirtualDeviceConfigSpec createNicSpec(String netName, String nicName) throws Exception
	{
		VirtualDeviceConfigSpec nicSpec = new VirtualDeviceConfigSpec();
		nicSpec.setOperation(VirtualDeviceConfigSpecOperation.add);
		
		VirtualEthernetCard nic =  new VirtualPCNet32();
		VirtualEthernetCardNetworkBackingInfo nicBacking = new VirtualEthernetCardNetworkBackingInfo();
		nicBacking.setDeviceName(netName);
		
		Description info = new Description();
		info.setLabel(nicName);
		info.setSummary(netName);
		nic.setDeviceInfo(info);
		nic.setAddressType("generated");
	    nic.setBacking(nicBacking);
	    nic.setKey(0);
	   
	    nicSpec.setDevice(nic);
	    return nicSpec;
	}
}