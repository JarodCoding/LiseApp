<?php
namespace LiseAppServer\DataInterface;
use TYPO3\CMS\Core\SingletonInterface;
class UserAdapter{
	
	public function getUser($username){
		$GLOBALS['TSFE']->fe_user->checkPid = ''; //do not use a particular pid
		$info = $GLOBALS['TSFE']->fe_user->getAuthInfoArray();
		return $GLOBALS['TSFE']->fe_user->fetchUserRecord($info['db_user'], $username);
	}

	
	public function checkUser($username, $password) {
		$check = FALSE;
		$loginData = array(
				'username' => $username,
				'uident_text' => $password,
				'status' => 'login',
		);
		$user = getUser($loginData['username']);
		if (isset($user) && $user != '') {
			$authBase = new tx_saltedpasswords_sv1();
			$check = $authBase->compareUident($user, $loginData);//check wheather the password is ok. With salt OMNOMNOMNOM
		} else {
			//user does not exists
			$check = FALSE;
		}

		return $check;
		
	}
}
$Users = new UserAdapter();