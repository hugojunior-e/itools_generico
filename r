rm dist/itools.war
cd webapp
/algar/ferramentas/jdk1.7.0_80/bin/jar -cvf ../dist/itools.war *
cd ..
cp dist/itools.war /algar/ferramentas/jboss-as-7.1.1.Final/standalone/deployments/