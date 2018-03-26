<!DOCTYPE HTML>  
<html>
<body>  


<?php
if ($_SERVER["REQUEST_METHOD"] == "POST") {

# The first entry in _POST is the destination port number

$port=0;


# The updated schedule is sent as POST data
# Write this to a tmp file

$sfilename="/var/tmp/SchedulerCommit.txt";
$sfile = fopen($sfilename, "w") or die("Unable to open file $sfilename on server");
$nrlines=0;
foreach($_POST as $x => $value) {
if ($nrlines==0) { # The first entry in _POST is the destination port number
 $port=$value;
} else {
 fwrite($sfile, "$x:$value\n");
}
$nrlines++;
}
echo "php received " . $nrlines . " lines" . " for port " . $port;
echo "<br>";
fclose($sfile);

# Call the client to read this file and pass the updated schedule to the server

exec("/home/pi/git/Scheduler/run/startClient verbosity=0 server=localhost port=$port command=SchedulerCommit", $lines);

for($x = 0; $x < count($lines); $x++) {
    echo $lines[$x];
    echo "<br>";
}
}
?>


</body>
</html>


