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
	public function getAllGroups($username){
		$mainGroups = $this->getGroups($username);
		$res = $mainGroups;
		foreach($mainGroups as $currentSubGroup) {
			$res =  $this->listSubgroups($currentSubGroup,$res);
		}
		return array_unique($res,SORT_NUMERIC);
		}
	protected function listSubgroups($group,$arr=array()){
		
		$subGroups = $this->getSubGroups($group);
		print($subGroups);
		if(!isset($subGroups)||empty($subGroups))return $arr;
		$res = array_merge($arr,$subGroups);
		foreach ($subGroups as $currentSubGroup) {
			$res = $this->listSubgroups($currentSubGroup,$res);
		}
		return $res;
		
	}
	public function getGroups($username){
		$tmp = $this->getUser($username)['usergroup'];
		if(strlen($tmp)>1){
			return explode(',',$tmp);
		}else{
			return $tmp;
		}
	}
	public function getSubGroups($group){
 			$query = $GLOBALS['TYPO3_DB']->exec_SELECTquery("subgroup",'fe_groups',"uid=".$group);
 			if(!$query)return null;
 			$raw = $GLOBALS['TYPO3_DB']->sql_fetch_assoc($query);
 			if(!$raw||!isset($raw)||empty($raw)||!$raw['subgroup']||!isset($raw['subgroup'])||empty($raw['subgroup']))return null;
 			$res = explode(',',$raw['subgroup']);
 			if(!$res||!isset($res)||empty($res))return null;
 			return $res;
				
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

