mvn deploy:deploy-file -Dfile=.m2/repository/org/eclipse/jgit/org.eclipse.jgit/4.1.0.201509280440-r/org.eclipse.jgit-4.1.0.201509280440-r.jar -Durl=http://localhost:8081/nexus/content/repositories/thirdparty/ -DartifactId=org.eclipse.jgit -Dversion=4.1.0 -DgroupId=org.eclipse -DremoteOBR -DrepositoryId=nexuslocal


mvn deploy:deploy-file -DremoteOBR -DrepositoryId=nexuslocal-Durl=http://localhost:8081/nexus/content/repositories/thirdparty/


