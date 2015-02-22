<?php
namespace LiseAppServer\Managers;
require_once  dirname(dirname(__FILE__)).'\DataInterfaces\UserAdapter.php';
require_once  'OAuth.php';

		

use LiseAppServer\DataInterface;
use LiseAppServer\DataInterface\UserAdapter;




class SecurityManager extends \OAuthDataStore {
	protected static $instance;
	protected static $timestamp_threshold= 300; // in seconds: five minutes


	
	function lookup_consumer($consumer_key) {
		$passwordHash = UserAdapter::getInstance()->getPassword($consumer_key);
		if($passwordHash)
			return new \OAuthConsumer($consumer_key, $passwordHash);

	}
	
	function lookup_token($consumer, $token_type, $token) {
		return new \OAuthToken("", "");
	}
	
	function lookup_nonce($consumer, $token, $nonce, $timestamp) {
// 		$now = time();
// 		if (abs($now - $timestamp) > self::$timestamp_threshold) {
// 			throw new \OAuthException(
// 					"Expired timestamp, yours $timestamp, ours $now"
// 			);
// 		}
// 		\apc_exists($nonce."_nonce");
// 		if($res){
// 			return \apc_fetch($nonce."_nonce");
// 		}
// 		\apc_add(($nonce."_nonce"), $timestamp,self::$timestamp_threshold);
//  		return null;
		
	}
	//Not used at the moment
	public static function generateSecret($str){
		$raw = $consumerKey;
		$i;
		$tmp;
		while($i < mt_rand(5, 23)){
			$tmp = mt_rand(10, 50)/mt_rand(1, 25);		
			if($tmp <= 1){
				$raw = mt_rand()*mt_rand().$raw;
			}else if($tmp <= 2){
				$raw = $raw.mt_rand()%mt_rand().$raw;
			}else if($tmp <= 3){
				$raw = $consumerKey.microtime()*314159/mt_rand(0, 314159).$raw;
			}else if($tmp <= 4){
				$raw = microtime()*mt_rand().$raw;
			}else if($tmp <= 4){
				$raw = microtime().$raw.microtime()/mt_rand()*microtime().$raw;
			}
			if(mt_rand(0,1))md5($raw);
			$i++;
		}
		md5($raw);
	}
	function new_request_token($consumer, $callback = null) {
	}
	
	function new_access_token($token, $consumer, $verifier = null) {
		
	}
	function securityCheck(){
		$OAuthData;
		foreach($_SERVER as $key => $value) {
		
			if (substr($key, 0, 5) <> 'HTTP_') {
				continue;
			}
			$header = str_replace(' ', '-', ucwords(str_replace('_', ' ', strtolower(substr($key, 5)))));
			if($header=='Authorization')
				return $this->securityCheckFromHeader($value);
		}
		return "Authorization header is Missing!";
	}
	protected function securityCheckFromHeader($OAuthHeader){
		try {
			//Create Request
					
				$scheme = (!isset($_SERVER['HTTPS']) || $_SERVER['HTTPS'] != "on")
				? 'http'
						: 'https';
				$http_url = ($http_url) ? $http_url : $scheme .
				'://' . $_SERVER['SERVER_NAME'] .
				':' .
				$_SERVER['SERVER_PORT'] .
				$_SERVER['REQUEST_URI'];
				$http_method = ($http_method) ? $http_method : $_SERVER['REQUEST_METHOD'];
				$OAuthRequest = new \OAuthRequest($http_method,$http_url,\OAuthUtil::split_header($OAuthHeader));
			//Verify Request
				self::getServerInstance()->verify_request($OAuthRequest);
		} catch (Exception $e) {
			return $e;
		}
		return null;
	}
	//I know this is ugly and basicly oauth without securty but salted password ruins everything with randomly generated hashes
	//SSL/TLS required
	public function login($username,$password,$nonce,$timestamp){
		if($this->lookup_nonce(null, null, nonce,$timestamp)!=null)
			throw new \OAuthException("Nonce: ".$nonce." expired!");
		if(!UserAdapter::getInstance()->verifyUserData($username, $password))
			throw new \OAuthException("Invalid username/password combination");
		$hash = UserAdapter::getInstance()->getPassword($username);
		return $hash;
	}
	
	
	public static function getInstance() {
		if (self::$instance == null) {
			self::$instance = new SecurityManager();
		}
		return self::$instance;
	}
	private static $ServerInstance;
	private static function getServerInstance(){
		if (self::$ServerInstance == null) {
			self::$ServerInstance = new \OAuthServer(self::getInstance());
			self::$ServerInstance->add_signature_method(new \OAuthSignatureMethod_HMAC_SHA1());
		}
	return self::$ServerInstance;
	}
	
}


