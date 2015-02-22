<?php
namespace LiseAppServer\Managers;
use LiseAppServer\DataInterface;
use LiseAppServer\DataInterface\UserAdapter;
require 'Classes/Managers/SessionManager.php';
require 'Classes/DataInterfaces/UserAdapter.php';
class SecurityManager{
	const logged_out = 'none';
	
	public function login($username,$password){
		global $Users;
		$res = $Users.checkUsers($username,$password);
		if(res){
			global $Session;
			$Session.setValueForKey("user",$username);
			return TRUE;
		}
		return FALSE;
		
	}
	public function logout($username,$password){
		global $Session;
		$Session.setValueForKey("user",self::logged_out);
	}
	public function checkAcces($groups){
	
	}
	public function getUser(){
		global $Session;
		$user = $Session->valueForKey('$user');
		if ($user === NULL||$user == self::logged_out) {
			$user = logged_out;
			return NULL;
		}
		global $Users;
		return $Users.getUser($user);
	}
	public function getUsername(){
		global $Session;
		$user = $Session->valueForKey('$user');
		if ($user === NULL) {
			$user = logged_out;
		}
		return $user;
	}
}
$Security = new SecurityManager();