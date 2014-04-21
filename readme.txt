README
This project is managed using maven.

#Installing Maven on Linux
	- Please refer to (http://maven.apache.org/download.cgi) for download and install instructions. In unix like environment it is as simple as extracting the tar then including the bin folder to PATH variable.
	- For example in any of the net machine you can install maven using the following instructions.
		$ mkdir lib
		$ cd lib
		$ wget http://apache.mesi.com.ar/maven/maven-3/3.1.1/binaries/apache-maven-3.1.1-bin.tar.gz
		$ tar xvvzf apache-maven-3.1.1-bin.tar.gz

	- Now add the following lines in ~/.bashrc file
		export MAVEN_HOME="$HOME/lib/apache-maven-3.1.1"
		export PATH="$MAVEN_HOME/bin:$PATH"
	- Then source the .bashrc or simple logout then login you are good to go.

#Compile
	- To compile a maven based project, first go to bin
		$ mvn clean compile

#Execution using Maven
	#Generating Keys:
		mvn exec:java -Dexec.mainClass="edu.utdallas.netsec.sfts.Initializer" -Dexec.args="keys data/staging/auth_server/as.properties"

	# Running Authentication Server
		mvn exec:java -Dexec.mainClass="edu.utdallas.netsec.sfts.as.AuthenticationServer" -Dexec.args="data/staging/auth_server/"

	# Running Department (showing example for finance. You can execute marketing and payroll department similarly)
		mvn exec:java -Dexec.mainClass="edu.utdallas.netsec.sfts.ds.DepartmentServer" -Dexec.args="data/staging/dept_finance/"

	# Running Master
		mvn exec:java -Dexec.mainClass="edu.utdallas.netsec.sfts.master.MasterFileServer" -Dexec.args="data/staging/master_server/"

	# Running Client (showing example for client bumblebee for department finance. You can execute other departments and client ironhide similarly)
		mvn exec:java -Dexec.mainClass="edu.utdallas.netsec.sfts.client.Client" -Dexec.args="data/staging/client_bumblebee/ finance test.txt"