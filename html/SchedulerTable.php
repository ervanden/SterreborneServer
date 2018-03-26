

<style type="text/css">
.tg  {border-collapse:collapse;border-spacing:0;}
.tg td{font-family:Arial, sans-serif;font-size:14px;padding:10px 5px;border-style:solid;border-width:1px;overflow:hidden;word-break:normal;}
.tg th{font-family:Arial, sans-serif;font-size:14px;font-weight:normal;padding:10px 5px;border-style:solid;border-width:1px;overflow:hidden;word-break:normal;}
.tg .white{vertical-align:top}
.tg .blue{background-color:#33fcff;vertical-align:top}
.tg .darkblue{background-color:#0004ff;vertical-align:top}
.tg .red{background-color:#ffccc9;vertical-align:top}
.tg .darkred{background-color:#ff0000;vertical-align:top}
</style>

<?php

if ($_SERVER["REQUEST_METHOD"] == "POST") {

$port=$_POST['port'];

exec("/home/pi/git/Scheduler/run/startClient verbosity=0 server=localhost port=$port command=SchedulerTable", $lines);

echo '<table class="tg" id="schedule">';
echo "\n";
$linenr=0;
$y=0;  // vertical coordinate in the table
for ($d = 0; $d <= 6; $d++) {
 $day=$lines[$linenr];
 $linenr++;
 echo "  <tr id=\"$day\">\n";
 echo " <td class=\"white\">$day</td>\n";
 $x=0;  // horizontal coordinate in the table
 for ($h = 0; $h <= 23; $h++) {
  for ($m = 0; $m <= 45; $m=$m+15) {
   $words = explode(" ", $lines[$linenr]);
   $linenr++;
   if (($words[6] == "true")&& ($words[7] == "true")) { $color="darkred";}
   if (($words[6] == "true")&& ($words[7] == "false")) { $color="red";}
   if (($words[6] == "false")&& ($words[7] == "true")) { $color="darkblue";}
   if (($words[6] == "false")&& ($words[7] == "false")) { $color="blue";}
   $hstring=$h;
   if ( $m == 0 ) 
     { $mstring="00";}
   else  
     { $mstring=$m;}
   echo "    <td id=\"$x:$y\" class=\"$color\" onmousedown=\"doDown(this)\"  onmouseenter=\"doEnter(this)\" >$hstring:$mstring</td>\n";
   $x=$x+1;
  }
 } 
echo "  </tr>\n";
$y=$y+1;
}
echo "</table>\n";
}
?>





