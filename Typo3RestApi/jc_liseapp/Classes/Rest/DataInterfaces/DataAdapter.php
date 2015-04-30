<?php
namespace LiseAppServer\DataInterface;

require_once  dirname(dirname(__FILE__)).'\Security\SecurityManager.php';
use LiseAppServer\Managers;

class DataAdapter{
	public $defaultTable;
	public function generateEnabledCondition($table=null){
		if($table == null)$table = $this->defaultTable;
		$time = time();
		return
		"AND ".
		"(".$table.".deleted=0 ".
		"AND ".$table.".t3ver_state<=0 ".
		"AND ".$table.".hidden=0 ".
		"AND ".$table.".starttime<=".$time." ".
		"AND (".$table.".endtime=0 OR ".$table.".endtime>".$time.") ";
	}
	function __construct($table) {
		$this->defaultTable = $table;
	}
}
class SecuredDataAdapter extends DataAdapter{
	private $securityManager;
	public function getUserName(){
		return $securityManager->$username;
	}
   function __construct($table,$securityManager) {
       parent::__construct($table);
       $this->securityManager = $securityManager;
   }
	public function generateEnabledCondition($groups=null,$table=null){
		if($table == null)$table = $this->defaultTable;
		$groupsString = "";
		throw new \Exception(implode(",", $groups));
		if($groups != null){
			foreach($groups as $group){
				$groupsString .= " OR ".$table.".fe_group='".$group."' OR FIND_IN_SET('".$group."',".$table.".fe_group)";
				
			}
		}
		return
		parent::generateEnabledCondition($table).
		"AND (".$table.".fe_group='' OR ".$table.".fe_group IS NULL OR ".$table.".fe_group='0' OR FIND_IN_SET('0',".$table.".fe_group) OR FIND_IN_SET('-1',".$table.".fe_group) ".$groupsString."))";
		;
	}
	public function generateSecuredEnabledCondition($groups=null,$table=null){
		if($groups != null&&(!isset($groups)||empty($groups)))$groups = null;
		if($groups!=null)throw new \Exception("PHP IS BUGGY");
		return $this->generateEnabledCondition($groups==null?UserAdapter::getInstance()->getAllGroups($this->getUserName()):$groups,$table);
	}
   	 
}