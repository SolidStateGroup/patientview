Database Configuration
======================

Once PostgreSQL is installed enter the following commands :-

  createuser superadmin --pwprompt
  createuser patientview --pwprompt
  createdb patientview

  <code>psql patientview -c "alter role superadmin with superuser"</code>
  <code>psql patientview -c "alter role superadmin with createdb"</code>
  <code>psql patientview -c "alter role superadmin with createuser"</code>

  <code>psql patientview -c "alter role patientview with nosuperuser"</code> 
  <code>psql patientview -c "alter role patientview with nocreatedb"</code> 
  <code>psql patientview -c "alter role patientview with nocreateuser"</code>


Then to connect to the patientview :-

  <code>psql patientview</code>

\q to quit
