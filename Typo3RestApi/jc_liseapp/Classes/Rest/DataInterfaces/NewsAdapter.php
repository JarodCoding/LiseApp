<?php
namespace LiseAppServer\DataInterface;

use TYPO3\CMS\Core\SingletonInterface;
use LiseAppServer\Managers\SecurityManager;

class NewsAdapter{
	private static $instance;

	public function listAll($username,$fields,$sortBy='uid',$enabledConditions=null){
		if($enabledConditions=null){
			$query = self::buildQuery(null, $fields, true,UserAdapter::getInstance()->getAllGroups($username));
		}else{
			$query = self::buildQuery($enabledConditions, $fields, false);
				
		}
		$res = array();
		while ($currentNews = $GLOBALS['TYPO3_DB']->sql_fetch_assoc($query)) {
			$res[$currentNews[$sortBy]]=$currentNews;
		}

		return $res;
	}
	

	public function listAllReadable($username,$timestamp=null){
		$groups = UserAdapter::getInstance()->getAllGroups($username);			
		$query = self::buildQuery($timestamp!=null?"tstamp > ".$timestamp:null,"uid,title,categories,teaser,bodytext,datetime,endtime,author,fal_media",true,$groups);
		if(!$query||$query==null)throw new \Exception("query is null");
		$res = array();
		while ($current = $GLOBALS['TYPO3_DB']->sql_fetch_assoc($query)) {
			$current['image'] = $this->getImage($current['fal_media']);
			unset($current['fal_media']);
			$res[$currentNews['uid']]=$current;
		}
		if(sizeof($res)==0){
			throw new \Exception($query);
				
		}
		if($timestamp != null){
			$query = self::buildQuery("tstamp > ".$timestamp." AND !(1=1".self::generateEnabledCondition($groups).")","uid,title,categorys,teaser,bodytext,datetime,endtime,author,fal_media",false);
			while ($current = $GLOBALS['TYPO3_DB']->sql_fetch_assoc($query)) {
				$res[$currentNews['uid']]=$current;
			}
		}
		return $res;
	}
	public function getImage($fal_media){
		//Typo3 is a mess regarding files instead of just having one id and use that there is a general index table which indexes the file refernce table which references the actual file table which contains the real url so I simply created this function to hide the mess :D
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
		return "/fileadmin".$url;
		
	}
	public function details($uid){
		$query = self::buildQuery("uid=".$uid, "*", false);
		$res = $GLOBALS['TYPO3_DB']->sql_fetch_assoc($query);
		if(!$res||!isset($res)||empty($res))return null;
		return $res;

	}
	protected static function buildQuery($where,$fields,$checkEnabled=true,$groups=null){
		if($checkEnabled){
			$enabledCondition = self::generateEnabledCondition($groups);
			if($where){
				$where .= $enabledCondition;
			}else{
				$where = "1=1 ".$enabledCondition;
			}			
		}
		$query = $GLOBALS['TYPO3_DB']->exec_SELECTquery($fields,'tx_news_domain_model_news',$where);
		if(!$query||$query==null)throw new \Exception("query is null with condition: ".$where);
		return $query;
		
	}
	public static function generateEnabledCondition($groups=null){
		$groupsString = "";
		if($groups != null){
			foreach($groups as $group){
				$groupsString .= "OR tx_news_domain_model_news.fe_group='".$group."' OR FIND_IN_SET('".$group."',tx_news_domain_model_news.fe_group) ";
			}
		}
		$time = time();
		return
		"AND ". 
		 "(tx_news_domain_model_news.deleted=0 ".
		  "AND tx_news_domain_model_news.t3ver_state<=0 ".
		  "AND tx_news_domain_model_news.pid<>-1 ".
		  "AND tx_news_domain_model_news.hidden=0 ".
		  "AND tx_news_domain_model_news.starttime<=".$time." ".
		  "AND (tx_news_domain_model_news.endtime=0 OR tx_news_domain_model_news.endtime>".$time.") ".
		  "AND (tx_news_domain_model_news.fe_group='' OR tx_news_domain_model_news.fe_group IS NULL OR tx_news_domain_model_news.fe_group='0' OR FIND_IN_SET('0',tx_news_domain_model_news.fe_group) OR FIND_IN_SET('-1',tx_news_domain_model_news.fe_group) ".$groupsString."))";
	}
	public static function getInstance() {
		if (self::$instance == null) {
			self::$instance = new NewsAdapter();
		}
		return self::$instance;
	}
}
