# Automation
To learn new things is always better
@Test(enabled=true,dependsOnMethods= {"AWBCapture"}, groups = { "EY" }, priority = 2)
	 @IRaftTest(testCaseID = "TC_EY_02", testDesc = "Execute AWB", author = "Shaliya")
	public void ExecuteAWB() throws InterruptedException{
		login(gData("OPS_USERNAME"))
		.goToOPR026()
		.listUbr(iData("awb_bookedshipment"))
		.executeAWB().verifyAWBOnExecute("awb_bookedshipment", className).getAWBStatus("Executed").close();
		//.listUbr(iData("awb_bookedshipment"))
		.reListAWB(gData("AWB_PREFIX"),iData("awbno1"))
		.executeAWB().verifyAWBOnExecute("awbno1", className).getAWBStatus("Executed").close();
	}
