<?php

	// configuration
	require("includes/config.php");

	$response = [];
	// if form was submitted
	if ($_SERVER["REQUEST_METHOD"] == "POST")
	{

		if ($_POST["table"] == "sync")
		{
		  $queries[] = json_decode(urldecode($_POST["query"]), true);
		  $n = 0;
		  $bae_scriptures = [];
		  //if ()
		    foreach ($queries[0] as $query)
		    {
			    $resp = "error inserting";
			    // Insert into / update specific user
				if(query("INSERT INTO ".str_replace(".","_",str_replace("@", "_", $query["user_email"]))."(scripture, category, timestamp) VALUES (?,?,?) ON DUPLICATE KEY UPDATE timestamp = ?", $query["scripture"], $query["category"], $query["timestamp"], $query["timestamp"]) !== false)
				{
					$n++;
				}
			}
			if ($n > 0)
				$resp = "inserted";
			if (($bae = query("SELECT * FROM pb_bible_users WHERE user_email = ?", $queries[0][0]["user_email"])) !== false)
				if (($bae_scriptures = query("SELECT * FROM ".str_replace(".","_",str_replace("@", "_", $bae[0]["user_email"])))) !== false) {
					echo json_encode(["response" => $resp, "scriptures" => $bae_scriptures, "bae" => $bae]);
					exit;
				}
	//C:\Felix I-O\VoiceRecognition\sphinxbase\bin\Debug\x64>sphinx_fe -argfile en-us/feat.params \ -samprate 16000 -c arctic20.fileids \ -di . -do . -ei wav -eo mfc -mswav yes
			echo json_encode(["response" => "error"]);
			exit;
		}

		if ($_POST["table"] == "check")
		{
			echo json_encode(["response" => "ok"]);
		    exit;
		}
		if ($_POST["table"] == "bae_cancel")
		{
			$queries[] = json_decode(urldecode($_POST["query"]), true);
			$n = 0;
			$query = $queries[0];
			$bae = query("SELECT * FROM pb_bible_users WHERE user_email = ?", $query["user_email"]);
			if ($bae[0]["bae_confirm"] == 1) {
				echo json_encode(["response" => "confirmed", "bae" => $bae]);
				exit;
			}
			else if (query("UPDATE pb_bible_users SET bae_request = 0, bae_receipt = 0, bae_confirm = 0, bae_email = '' WHERE user_email = ?", $query["user_email"]) !== false)
				if (query("UPDATE pb_bible_users SET bae_request = 0, bae_receipt = 0, bae_confirm = 0, bae_email = '' WHERE user_email = ?", $query["bae_email"]) !== false)
				{
					echo json_encode(["response" => "deleted", "bae" => query("SELECT * FROM pb_bible_users WHERE user_email = ?", $query["user_email"])]);
					exit;
				}

			echo json_encode(["response" => "error"]);
			exit;
		}
		if ($_POST["table"] == "bae_request")
		{
			$queries[] = json_decode(urldecode($_POST["query"]), true);
			$n = 0;
			$query = $queries[0];

			$bae = query("SELECT * FROM pb_bible_users WHERE user_email = ?", $query["user_email"]);

			if ($bae[0]["bae_confirm"] == 1) {
				echo json_encode(["response" => "confirmed", "bae" => $bae]);
				exit;
			}
			else if (query("UPDATE pb_bible_users SET bae_email = ?, bae_request = 1 WHERE user_email = ?", $query["bae_email"], $query["user_email"]) !== false)
				if (query("UPDATE pb_bible_users SET bae_email = ?, bae_receipt = 1 WHERE user_email = ?", $query["user_email"], $query["bae_email"]) !== false)
				{
					$bae = query("SELECT * FROM pb_bible_users WHERE user_email = ?", $query["user_email"]);
					echo json_encode(["response" => "pending", "bae" => $bae]);
					exit;
				}
			echo json_encode(["response" => "error"]);
			exit;
		}

		if ($_POST["table"] == "bae_confirm")
		{
			$queries[] = json_decode(urldecode($_POST["query"]), true);
			$n = 0;
			$query = $queries[0];
			
			if (query("UPDATE pb_bible_users SET bae_request = 0, bae_receipt = 0, bae_confirm = 1 bae_email = ? WHERE user_email = ?", $query{"bae_email"}, $query["user_email"]) !== false)
				if (query("UPDATE pb_bible_users SET bae_request = 0, bae_receipt = 0, bae_confirm = 1 bae_email = ? WHERE user_email = ?", $query{"user_email"}, $query["bae_email"]) !== false)
				{
					echo json_encode(["response" => "confirmed", "bae" => query("SELECT * FROM pb_bible_users WHERE user_email = ?", $query["user_email"])]);
					exit;
				}

			echo json_encode(["response" => "error"]);
			exit;
		}

		if ($_POST["table"] == "create")
		{
			$queries[] = json_decode(urldecode($_POST["query"]), true);
			$query = $queries[0];
			$n = 0;
			if (query ("CREATE TABLE IF NOT EXISTS ". str_replace(".","_",str_replace("@", "_", $query["user_email"])) ." (id int(10) primary key auto_increment, scripture varchar(255), category varchar(255), timestamp varchar(255))") !== false)
			{
				if (query("INSERT INTO pb_bible_users(user_id, user_email) VALUES(?,?)", $query["user_id"], $query{"user_email"}) !== false)
				{
					echo json_encode(["response" => "user created"]);		   
					exit;
				}
			}
			else
			{
				echo json_encode(["response" => "user already exists", "bae"=> query("SELECT * FROM".str_replace(".","_",str_replace("@", "_", $query["user_email"])))]);		   
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
		    $query = urldecode($_POST["query"]);
			if (($user = query("SELECT * FROM pb_bible_users WHERE user_email = ?", $query)) !== false)
			{
			//$bookmarks = query("SELECT * FROM ".$query["user_email"]."WHERE category = 'bookmarks'");
			// $bae_sent = query("SELECT * FROM ".$query["user_email"]."WHERE category = 'bae_sent'");
			// $bae_received = query("SELECT * FROM ".$query["user_email"]."WHERE category = 'bae_received'");
			// echo json_encode(["user" => $user, "bae_sent" => $bae_sent, "bae_received" => $bae_received], JSON_PARTIAL_OUTPUT_ON_ERROR);
				if (($scriptures = query("SELECT * FROM ".str_replace(".","_",str_replace("@", "_", $query)))) !== false)
					echo json_encode(["response" => "ok", "user" => $user, "scriptures" => $scriptures], JSON_PARTIAL_OUTPUT_ON_ERROR);
				exit();
			}
			else{
				echo json_encode(["response" => "error"]);
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
