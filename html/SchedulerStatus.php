
<?php
if ($_SERVER["REQUEST_METHOD"] == "POST") {
$port=$_POST['port'];
exec("/home/pi/git/Scheduler/run/startClient verbosity=0 server=localhost port=$port command=SchedulerStatus", $lines);
echo $lines[0];
}
?>


