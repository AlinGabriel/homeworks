#!/bin/sh

clients=$(ls client/security)
server=server
for c in $clients; do
	C_alias=$c"_private"
	S_alias=$server"_private"
	C_certificate=client/security/$c/$c".crt"
	S_certificate=files_server/security/$server".crt"
	C_ks=client/security/$c/$c".ks"
	S_ks=files_server/security/$server".ks"
	C_pass=$c"_password"
	S_pass=$server"_password"
	# import the signed certificate of the client into the department server's keystore
	echo "------------------------------------------------------------------------------------------------------"
	echo "IMPORT the SIGNED certificate of the client '$c' into the server's keystore"
	echo "------------------------------------------------------------------------------------------------------"
	keytool -import -alias ${C_alias} -keypass ${S_pass} -keystore ${S_ks} -storepass ${S_pass} -trustcacerts -file ${C_certificate}

	# import the signed certificate of the department server into the client's keystore
	echo "------------------------------------------------------------------------------------------------------"
	echo "IMPORT the SIGNED certificate of the server '$c' client's keystore"
	echo "------------------------------------------------------------------------------------------------------"
	keytool -import -alias ${S_alias} -keypass ${C_pass} -keystore ${C_ks} -storepass ${C_pass} -trustcacerts -file ${S_certificate}
done