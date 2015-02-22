<?php
namespace LiseAppServer\DataInterface;


use TYPO3\CMS\Core\SingletonInterface;
class UserAdapter{
	private static $instance;
	
	public function getUser($username){
		$GLOBALS['TSFE']->fe_user->checkPid = ''; //do not use a particular pid
		$info = $GLOBALS['TSFE']->fe_user->getAuthInfoArray();
		return $GLOBALS['TSFE']->fe_user->fetchUserRecord($info['db_user'], $username);
	}

	
	public function getPassword($username) {
		$user = $this->getUser($username);
		
		if (isset($user) && $user != '') {
			return $user['password'];		
		} 		
	}
	public function verifyUserData($username,$password) {
		$user = $this->getUser($username);
		if (isset($user) && $user != '') {
			$authBase = \TYPO3\CMS\Saltedpasswords\Salt\SaltFactory::getSaltingInstance($user['password'],"FE");
			return $authBase->checkPassword($password,$user['password'] );
		}
		return false;
	}
	public static function getInstance() {
		if (self::$instance == null) {
			self::$instance = new UserAdapter();
		}
		return self::$instance;
	}
}
