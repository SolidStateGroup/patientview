Database Configuration
======================

Once PostgreSQL is installed enter the following commands :-

  createuser superadmin --pwprompt
  createuser patientview --pwprompt
  createdb patientview

  <code>psql patientview -c "alter role superadmin with superuser"</code><br>
  <code>psql patientview -c "alter role superadmin with createdb"</code><br>
  <code>psql patientview -c "alter role superadmin with createuser"</code><br>
<br>
  <code>psql patientview -c "alter role patientview with nosuperuser"</code><br>
  <code>psql patientview -c "alter role patientview with nocreatedb"</code><br>
  <code>psql patientview -c "alter role patientview with nocreateuser"</code><br>


Then to connect to the patientview :-

  <code>psql patientview</code>

\q to quit
