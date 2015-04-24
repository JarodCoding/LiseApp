<?php
namespace LiseAppServer\DataInterface;
require_once  'DataAdapter.php';

use TYPO3\CMS\Core\SingletonInterface;
use LiseAppServer\Managers\SecurityManager;

class NewsAdapter extends SecuredDataAdapter{


	public function listAll($username,$fields,$sortBy='uid'){
		$query = self::buildQuery(null, $fields);
		$res = array();
		while ($current = $GLOBALS['TYPO3_DB']->sql_fetch_assoc($query)) {
			$current['image'] = $this->getImage($current['fal_media']);
			unset($current['fal_media']);
			$res[$currentNews[$sortBy]]=$current;
		}

		return $res;
	}
	

	public function listAllReadable($timestamp=null){
		$query = self::buildQuery($timestamp!=null?"tstamp > ".$timestamp:null,"uid,title,categories,teaser,bodytext,datetime,endtime,author,fal_media");
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
			$query = self::buildQuery("tstamp > ".$timestamp." AND !(1=1".$this->generateSecuredEnabledCondition().")","uid,title,categorys,teaser,bodytext,datetime,endtime,author,fal_media",false);
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
	public function buildQuery($where,$fields,$checkEnabled=true,$groups=null){
		if($checkEnabled){
			$enabledCondition = $this->generateSecuredEnabledCondition($groups);
			if($where){
				$where .= $enabledCondition;
			}else{
				$where = "1=1 ".$enabledCondition;
			}			
		}
		$query = $GLOBALS['TYPO3_DB']->exec_SELECTquery($fields,'tx_news_domain_model_news',$where);
		throw new \Exception("query is null with condition: ".$where);
		if(!$query||$query==null)throw new \Exception("query is null with condition: ".$where);
		return $query;
		
	}
	function __construct($securityManager) {
		parent::__construct('tx_news_domain_model_news',$securityManager);
	}
	
}
