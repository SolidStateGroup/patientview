Database Configuration
======================

Once PostgreSQL is installed enter the following commands :-

createuser superadmin --pwprompt
createuser patientview --pwprompt
createdb patientview

psql patientview -c "alter role superadmin with superuser" 
psql patientview -c "alter role superadmin with createdb" 
psql patientview -c "alter role superadmin with createuser" 

psql patientview -c "alter role patientview with nosuperuser" 
psql patientview -c "alter role patientview with nocreatedb" 
psql patientview -c "alter role patientview with nocreateuser" 


Then to connect to the patientview :-

psql patientview

\q to quit
