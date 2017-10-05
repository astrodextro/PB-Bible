<?php

	// configuration
	require("includes/config.php");

	$response = [];
	// if form was submitted
	if ($_SERVER["REQUEST_METHOD"] == "POST")
	{

		// validate submission
		
		if ($_POST["table"] == "sync")
		{
			$queries[] = json_decode(urldecode($_POST["query"]), true);
		  $n = 0;
		  //if ()
		  foreach ($queries[0] as $query)
		  {
			  // Insert into / update specific user
				if(query("INSERT INTO ".str_replace("@", "_", $query["user_email"])."(scripture, category, timestamp) VALUES (?,?,?) ON DUPLICATE KEY UPDATE timestamp = ?", $query["scripture"], $query["category"], $query["timestamp"], $query["timestamp"]) !== false)
				{
					$n++;
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
		if ($_POST["table"] == "bae_request")
		{
			$queries[] = json_decode(urldecode($_POST["query"]), true);
			$n = 0;
			$query = $queries[0];
			$responses = query("SELECT * FROM".str_replace("@", "_", $query["user_email"]));

			if ($response !== false)
			{
				if ($response["bae_request"] == "confirmed")
				{
					echo json_encode(["response" => "confirmed"]);
					exit;
				}
				else
				{
					echo json_encode(["response" => "pending"]);
					exit;
				}
			}
			else
			{
				if (query("INSERT INTO pb_bible_users(bae_email, bae_request) VALUES(?,?) WHERE user_email = ?", $query["bae_email"], $query{"bae_request"}, $query["user_email"]) !== false)
					if (query("INSERT INTO pb_bible_users(bae_email, bae_receipt) VALUES(?,?) WHERE user_email = ?", $query["user_email"], $query{"bae_request"}, $query["bae_email"]) !== false)
					{
						echo json_encode(["response" => "pending"]);
						exit;
					}
			}
			echo json_encode(["response" => "error inserting"]);
			exit;
		}
		if ($_POST["table"] == "bae_confirm")
		{
			$queries[] = json_decode(urldecode($_POST["query"]), true);
			$n = 0;
			$query = $queries[0];
			$responses = query("SELECT * FROM".str_replace("@", "_", $query["user_email"]));

			if (query("INSERT INTO pb_bible_users(bae_request) VALUES(?) WHERE user_email = ?", $query{"bae_request"}, $query["user_email"]) !== false)
				if (query("INSERT INTO pb_bible_users(bae_email, bae_receipt) VALUES(?,?) WHERE user_email = ?", $query{"bae_request"}, $query["bae_email"]) !== false)
				{
					echo json_encode(["response" => "confirmed"]);
					exit;
				}

			echo json_encode(["response" => "error inserting"]);
			exit;
		}

		if ($_POST["table"] == "create")
		{
			$queries[] = json_decode(urldecode($_POST["query"]), true);
			$query = $queries[0];
			$n = 0;
			if (query ("CREATE TABLE IF NOT EXISTS ". str_replace("@", "_", $query["user_email"]) ." (id int(10) primary key autoincrement, scripture varchar(255), category varchar(255), timestamp varchar(255))") !== false)
			{
				if (query("INSERT INTO pb_bible_users(user_id, user_email) VALUES(?,?)", $query["user_id"], $query{"user_email"}) !== false)
				{
					echo json_encode(["response" => "user created"]);		   
					exit;
				}
			}
			else
			{
			echo json_encode(["response" => "user already exists", "bae"=> query("SELECT * FROM".str_replace("@", "_", $query["user_email"]))]);		   
			exit;
			}
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
		if ($_POST["table"] == "get")
		{
		    $query = $_POST["query"];
			if (query("SELECT * FROM pb_bible_users WHERE user_email = ?", $query) !== false)
			{
			//$bookmarks = query("SELECT * FROM ".$query["user_email"]."WHERE category = 'bookmarks'");
			// $bae_sent = query("SELECT * FROM ".$query["user_email"]."WHERE category = 'bae_sent'");
			// $bae_received = query("SELECT * FROM ".$query["user_email"]."WHERE category = 'bae_received'");
			// echo json_encode(["user" => $user, "bae_sent" => $bae_sent, "bae_received" => $bae_received], JSON_PARTIAL_OUTPUT_ON_ERROR);
			$user = query("SELECT * FROM pb_bible_users WHERE user_email = ?", $query);
				$scriptures = query("SELECT * FROM ".str_replace("@", "_", $query));
				echo json_encode(["user" => $user, "scriptures" => $scriptures], JSON_PARTIAL_OUTPUT_ON_ERROR);
				exit();
			}
			else{
				echo json_encode(["response" => "ok"]);
				exit();
			}
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
	}

?>
