<?php

	// configuration
	require("../includes/config.php");

	$response = [];
	// if form was submitted
	if ($_SERVER["REQUEST_METHOD"] == "POST")
	{

		// validate submission
		
		if ($_POST["table"] == "sync")
		{
			$queries[] = json_decode(urldecode($_POST["query"]), true);
		  $n = 0;
		  foreach ($queries[0] as $query)
		  {
			  // Insert into / update specific user
			  if(query("INSERT INTO ".$query["user_id"]."(msg_id, uprice, paid, ustreams, downloaded, urating, timestamp) VALUES (?,?,?,?,?,?) ON DUPLICATE KEY UPDATE paid = ?, ustreams = ?, downloaded = ?, urating = ?", $query["msg_id"], $query["price"], $query["paid"], $query["streams"], $query["downloaded"], $query["rating"], $query["timestamp"], $query["paid"], $query["streams"], $query["downloaded"], $query["rating"]) !== false)
				{
					$downloads = 0;
					$streams = 0;
					$purchases = 0;
					if ($query = query("SELECT * FROM ".$query["user_id"]) !== false)
					{
						foreach ($query as $resp)
						{
							$downloads += $resp["downloaded"];
							$streams += $resp["ustreams"];
							if ($resp["uprice"] == $resp["paid"])
								$purchases += 1;
						}

						// update users table
						if(query("UPDATE users SET downloads = downloads + ?, streams = streams + ?, purchases = purchases + ? WHERE id = ?", $downloads, $streams, $purchases, $query["user_id"]) !== false)
						{
							$n++;
						}			
					}
				}
			}

		  if ($n > 0)
				echo json_encode(["response" => "ok"]);
		   
			exit;
		}

		if ($_POST["table"] == "check")
		{
				echo json_encode(["response" => "ok"]);
		   
		  exit;
		}

		if ($_POST["table"] == "create")
		{
			$queries[] = json_decode(urldecode($_POST["query"]), true);
			$query = $queries[0];
			$n = 0;
			if (query ("CREATE TABLE IF NOT EXISTS ". $query["user_id"] ." (msg_id int(10) primary key, uprice int(10), paid int(10), ustreams int(10), downloaded int(10), urating int(10), timestamp varchar(255))") !== false)
				if (query("INSERT INTO users(id, email) VALUES(?,?)", $query["user_id"], $query{"email"}) !== false)
					$n++;

//		  if ($n > 0)
				echo json_encode(["response" => "ok"]);
		   
		  exit;
			// $queries[] = json_decode(urldecode($_POST["query"]), true);
			 
			 // query("ALTER TABLE `users` ADD `downloaded` INT( 10 ) NOT NULL ,
// ADD `streamed` INT( 10 ) NOT NULL ,
// ADD `purchased` INT( 10 ) NOT NULL ;
// ");
		}
		
		if ($_POST["table"] == "log")
		{
		   $queries[] = json_decode(urldecode($_POST["query"]), true);
		   //var_dump($queries[0]);
		   $n = 0;
		   foreach ($queries[0] as $query)
		   {
			   //var_dump($query);
			   if(query("INSERT INTO log(user_id, event, timestamp) VALUES (?,?,?)", $query["user_id"], $query["event"], $query["timestamp"]) !== false)
				   $n++;
		   }

		  if ($n > 0)
				echo json_encode(["response" => "ok"]);
		   
		   exit;
		}
		if ($_POST["table"] == "partial")
		{
			 $queries[] = json_decode(urldecode($_POST["query"]), true);
			 //var_dump($queries[0]);
			 $n = 0;
			 $query = $queries[0][0];
				 //var_dump($query);
				 if(query("UPDATE ". $query["user_id"]." SET paid = ?, timestamp = ? WHERE msg_id = ?", $query["paid"], $query["timestamp"], $query["msg_id"]) !== false)
					 $n++;
				 // if(query("UPDATE preachers SET purchases = purchases + ? WHERE id = ?", $query["purchases"], $query["id"]) !== false)
					 // $n++;

		  if ($n == 1)
				echo json_encode(["response" => "ok"]);
		   
				exit;
		}
		if ($_POST["table"] == "complete")
		{
			 $queries[] = json_decode(urldecode($_POST["query"]), true);
			 //var_dump($queries[0]);
			 $n = 0;
			 $query = $queries[0][0];
				 //var_dump($query);
				if(query("INSERT INTO ".$query["user_id"]."(msg_id, uprice, paid, ustreams, downloaded, urating, timestamp) VALUES (?,?,?,?,?,?,?) ON DUPLICATE KEY UPDATE paid = ?, timestamp = ?",  $query["msg_id"], $query["price"], $query["paid"], $query["streams"], $query["downloaded"], $query["rating"], $query["timestamp"], $query["paid"], $query["timestamp"]) !== false)
					if(query("UPDATE users SET purchases = purchases + 1 WHERE id = ?", $query["user_id"]) !== false)
						if(query("UPDATE allmessages SET purchases = purchases + 1 WHERE id = ?", $query["msg_id"]) !== false)
							if(query("UPDATE preachers SET purchases = purchases + 1 WHERE id = ?", $query["preacher_id"]) !== false)
								$n++;
				 // if(query("UPDATE preachers SET purchases = purchases + ? WHERE id = ?", $query["purchases"], $query["id"]) !== false)
					 // $n++;

		  if ($n > 0)
				echo json_encode(["response" => "ok"]);
		   
			exit;
		}
		if ($_POST["table"] == "streams")
		{
			$queries[] = json_decode(urldecode($_POST["query"]), true);
			$n = 0;
			$query = $queries[0][0];
				if(query("UPDATE ".$query["user_id"]." SET ustreams = ustreams + ? WHERE msg_id = ?", $query["streams"], $query["msg_id"]) !== false)
					if(query("UPDATE users SET streams = streams + ? WHERE id = ?", $query["streams"], $query["user_id"]) !== false)
						if(query("UPDATE allmessages SET streams = streams + ? WHERE id = ?", $query["streams"], $query["msg_id"]) !== false)
							if(query("UPDATE preachers SET streams = streams + 1 WHERE id = ?", $query["preacher_id"]) !== false)
								$n++;

		  if ($n > 0)
				echo json_encode(["response" => "ok"]);
		   
			exit;
		}
		if ($_POST["table"] == "downloaded")
		{
			 $queries[] = json_decode(urldecode($_POST["query"]), true);
			 //var_dump($queries[0]);
			 $n = 0;
			 $query = $queries[0];
				 //var_dump($query);
				if(query("UPDATE ".$query["user_id"]." SET downloaded = ? WHERE msg_id = ?", $query["downloaded"], $query["msg_id"]) !== false)
					if(query("UPDATE users SET downloads = downloads + ? WHERE id = ?", $query["downloads"], $query["user_id"]) !== false)
						if(query("UPDATE allmessages SET downloads = downloads + ? WHERE id = ?", $query["downloads"], $query["msg_id"]) !== false)
							if(query("UPDATE preachers SET downloads = downloads + 1 WHERE id = ?", $query["preacher_id"]) !== false)
								$n++;
				 // if(query("UPDATE preachers SET purchases = purchases + ? WHERE id = ?", $query["purchases"], $query["id"]) !== false)
					 // $n++;

		  if ($n > 0)
				echo json_encode(["response" => "ok"]);
		   
			exit;
		}
		if ($_POST["table"] == "rating")
		{
			 $queries[] = json_decode(urldecode($_POST["query"]), true);
			 //var_dump($queries[0]);
			 $n = 0;
			 $query = $queries[0];
				 //var_dump($query);
				if(query("UPDATE ".$query["user_id"]." SET urating = ? WHERE msg_id = ?", $query["rating"], $query["msg_id"]) !== false)
					if(query("UPDATE allmessages SET rating = rating + ? WHERE id = ?", $query["rating"], $query["msg_id"]) !== false)
							$n++;
				 // if(query("UPDATE preachers SET purchases = purchases + ? WHERE id = ?", $query["purchases"], $query["id"]) !== false)
					 // $n++;

		  if ($n > 0)
				echo json_encode(["response" => "ok"]);
		   
			exit;
		}

		if ($_POST["table"] == "messages")
		{
			if (($response = query("SELECT allmessages.*, ".$_POST["query"].".* FROM allmessages LEFT JOIN ".$_POST["query"]." ON allmessages.id = ".$_POST["query"].".msg_id")) === false)
				$response = query("SELECT * FROM allmessages");

			echo json_encode(["response" => $response], JSON_PARTIAL_OUTPUT_ON_ERROR);
			exit;
		}
		if ($_POST["table"] == "preachers")
		{
			$response = query("SELECT * FROM preachers");
			echo json_encode(["response" => $response], JSON_PARTIAL_OUTPUT_ON_ERROR);
			exit;
		}
		if ($_POST["table"] == "events")
		{
			$response = query("SELECT * FROM events WHERE is_valid = 1");
			echo json_encode(["response" => $response], JSON_PARTIAL_OUTPUT_ON_ERROR);
			exit;
		}
		if ($_POST["table"] == "declarations")
		{
			$response = query("SELECT * FROM declarations");
			echo json_encode(["response" => $response], JSON_PARTIAL_OUTPUT_ON_ERROR);
			exit;
		}
		if ($_POST["table"] == "search")
		{
			$response = query("SELECT * FROM messages");
		      if (isset($_POST["query"]))
		      {
		        $msg["extra"] = $_POST["title"];
		        $keywords = preg_split("/[\s,]+/", $_POST["query"]);
		        $_POST["query"] = implode("|", $keywords);
		        $rows = query("SELECT * FROM messages WHERE title REGEXP ? ORDER BY id DESC", strtoupper($_POST["query"]));
		      }
			echo json_encode(["response" => $response], JSON_PARTIAL_OUTPUT_ON_ERROR);
			exit;
		}
	}
	else if ($_SERVER["REQUEST_METHOD"] == "GET")
	{
			 $_POST = ["query" => "user"];
			if (($response = query("SELECT allmessages.*, ".$_POST["query"].".* FROM allmessages LEFT JOIN ".$_POST["query"]." ON allmessages.id = ".$_POST["query"].".msg_id")) === false)
				$response = query("SELECT * FROM allmessages");
			// if (($response = query("SELECT allmessages.*, ".$query["query"].".* FROM allmessages JOIN ".$query["query"]." ON allmessages.id = ".$query["query"].".product_id")) === false)
				// $response = query("SELECT * FROM allmessages");
		// $response = query("SELECT * FROM preachers");
		// $res = array();
		// $start = 0;
		// $end = 0;
		// $count = count($response);
		// if ($start + 5 > $count)
			// $end = $count;
		// else
			// $end = $start + 5;
		
		// for($i = $start; $i < $end; $i++, $start++)
			// $res[$i] = $response[$i];
		// print_r($res);
		// exit;
			echo json_encode(["response" => $response], JSON_PARTIAL_OUTPUT_ON_ERROR);
			exit;

	}


			

/* 	DROP FUNCTION IF EXISTS rownum;

DELIMITER $$

CREATE FUNCTION rownum()

  RETURNS int(11)

BEGIN

  set @prvrownum=if(@ranklastrun=CURTIME(6),@prvrownum+1,1);

  set @ranklastrun=CURTIME(6);

  RETURN @prvrownum;

END $$

$$

DELIMITER ;
 */
?>
