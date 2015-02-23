<?php
namespace LiseAppServer\DataInterface;

use TYPO3\CMS\Core\SingletonInterface;
use LiseAppServer\Managers\SecurityManager;

class NewsAdapter{
	private static $instance;

	public function listAll($username,$fields,$category=null,$sortBy='uid'){
		$groups = UserAdapter::getInstance()->getAllGroups($username);
		$groupsString = "fe_group LIKE '%".implode("%' OR fe_group LIKE '%", $groups)."%'";
		
		$where = $groupsString.($category!=null?"AND (categories LIKE %".$username."%)":"");
		$query = self::buildQuery($where, $fields, true);
		if(!$query)throw new \Exception("".mysql_error().": ".$where);
		$res = array();
		while ($currentNews = $GLOBALS['TYPO3_DB']->sql_fetch_assoc($query)) {
			$res[$currentNews[$sortBy]]=$currentNews;
		}

		return $res;
	}
	

	public function listAllReadable($username,$category=null){
		$res = $this->listAll(SecurityManager::getInstance()->username,"uid,title,teaser,bodytext,datetime,author,fal_media",$category);
		foreach ($res as $current){
			$current['image'] = $this->getImage($current['fal_media']);
			unset($current['fal_media']);
			$res[$current['uid']] = $current;
		}
		return $res;
	}

	public function getImage($fal_media){
		//Typo3 is a mess regarding files instead of just having one id and use that there is a general index table which indexes the file refernce table which references the actual file table which contains the real url
		$where = "ref_table = 'sys_file_reference' AND tablename = 'tx_news_domain_model_news' AND field = 'fal_media' AND recuid = ".intval ($fal_media);
		$query = $GLOBALS['TYPO3_DB']->exec_SELECTquery("ref_uid",'sys_refindex',$where);
		$referenceId = $GLOBALS['TYPO3_DB']->sql_fetch_assoc($query)['ref_uid'];
		if(!$referenceId||!isset($referenceId)||empty($referenceId)) return null;
		$query = $GLOBALS['TYPO3_DB']->exec_SELECTquery("uid_local",'sys_file_reference', "uid = ".intval ($referenceId));
		$locaFileID = $GLOBALS['TYPO3_DB']->sql_fetch_assoc($query)['uid_local'];
		if(!$locaFileID||!isset($locaFileID)||empty($locaFileID))return null;
		$query = $GLOBALS['TYPO3_DB']->exec_SELECTquery("identifier",'sys_file', "uid = ".intval ($locaFileID));
		$url = $GLOBALS['TYPO3_DB']->sql_fetch_assoc($query)['identifier'];
		if(!$url||!isset($url)||empty($url)) null;
		return $url;
		
	}
	public function details($uid){
		$query = self::buildQuery("uid=".$uid, "*", false);
		$res = $GLOBALS['TYPO3_DB']->sql_fetch_assoc($query);
		if(!$res||!isset($res)||empty($res))return null;
		return $res;

	}
	protected static function buildQuery($where,$fields,$checkEnabled){
		if($checkEnabled){
			$enabledCondition = $GLOBALS['TSFE']->sys_page->enableFields('tx_news_domain_model_news', false, array());
			if($where){
				$where .= $enabledCondition;
			}else{
				$where = "1=1".$enabledCondition;
			}			
		}
		$query = $GLOBALS['TYPO3_DB']->exec_SELECTquery($fields,'tx_news_domain_model_news',$where);
		if(!$query)return null;
		return $query;
		
	}
	public static function getInstance() {
		if (self::$instance == null) {
			self::$instance = new NewsAdapter();
		}
		return self::$instance;
	}
}