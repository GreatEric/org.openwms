'use strict';

/*
 * openwms.org, the Open Warehouse Management System.
 *
 * This file is part of openwms.org.
 *
 * openwms.org is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * openwms.org is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software. If not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

/**
 * A RolesCtrl backes the 'Roles Management' screen.
 *
 * @module openwms_app
 * @author <a href="mailto:scherrer@openwms.org">Heiko Scherrer</a>
 * @version $Revision: $
 * @since 0.1
 */
angular.module('openwms_app',['ui.bootstrap', 'ngAnimate', 'toaster'])
	.config(function ($httpProvider) {
		delete $httpProvider.defaults.headers.common['X-Requested-With'];
		$httpProvider.defaults.headers.common['Content-Type'] = 'application/json';
	})
	.controller('RolesCtrl', function ($scope, $http, $modal, $log, rolesService, toaster) {

		var checkedRows = [];
		var roleEntities = [];

		var ModalInstanceCtrl = function ($scope, $modalInstance, data) {
			$scope.role = data.role;
			$scope.dialog = data.dialog;
			$scope.ok = function () {
				$modalInstance.close($scope.role);
			};
			$scope.cancel = function () {
				$modalInstance.dismiss('cancel');
			};
		};

		/**
		 * Add Role with edit dialogue.
		 */
		$scope.addRole = function () {
			if (roleEntities.length == 0) {
				$scope.loadRoles();
			}
			var modalInstance = $modal.open({
				templateUrl: 'addRolesDlg.html',
				controller: ModalInstanceCtrl,
				resolve: {
					data : function() {
						return {
							role : {
								description : ""
							}, dialog : {
								title: "Create new Role"
							}
						};
					}
				}
			});
			modalInstance.result.then(
				function (role) {
					rolesService.add($scope, role).then(
						function(addedRole) {
							$scope.roleEntities.push(addedRole);
						},
						function(data) {
							toaster.pop('error', "Server Error","["+data.items[0].httpStatus+"] "+data.items[0].message);
						}
					)
				}
			);
		};

		/**
		 * Edit selected Role with edit dialogue.
		 *
		 * @param row The row index of the selected Role
		 */
		$scope.editRole = function (row) {
			var modalInstance;
			modalInstance = $modal.open({
				templateUrl: 'addRolesDlg.html',
				controller: ModalInstanceCtrl,
				resolve: {
					data: function () {
						return {
							role: $scope.roleEntities[row],
							dialog: {
								title: "Edit Role"
							}
						};
					}
				}
			});
			modalInstance.result.then(
				function (role) {
					rolesService.save($scope, role).then(
						rolesSaved, function(data) {
							onError(data.items[0].httpStatus, data.items[0].message);
						}
					)
				}
			);
		};

		/**
		 * Delete a collection of selected Roles.
		 */
		$scope.deleteRole = function () {
			if ($scope.checkedRoles().length == 0) {
				return;
			}
			rolesService.delete($scope, $scope.checkedRoles()).then(
				function() {
					onSuccess("OK", "Deleted selected Roles.");
					$scope.loadRoles();
				}, function(data) {
					toaster.pop("error", "Server Error", "["+data.items[0].httpStatus+"] "+data.items[0].message);
				}
			);
		}

		/**
		 * To be implemented
		 */
		$scope.saveRole = function () {

			/**
			rolesService.save($scope).then(
				rolesSaved, function(data) {
					onError(data.items[0].httpStatus, data.items[0].message);
				}
			);
			 */
		}

		/**
		 * Load all Roles and store then in the model.
		 */
		$scope.loadRoles = function () {
			checkedRows = [];
			rolesService.getAll($scope).then(
				function(roles) {
					$scope.roleEntities = roles;
				}, function(data) {
					toaster.pop("error", "Server Error", "["+data.items[0].httpStatus+"] "+data.items[0].message);
				}
			);
		}

		/**
		 * When a Role is selected, the table of Grants according to this Role is updated.
		 *
		 * @param row The row index of the selected Role
		 */
		$scope.onRoleSelected = function (row) {
			$scope.selectedRole = $scope.roleEntities[row];
			$scope.page = 1;
			if ($scope.selectedRole.grants == undefined) {
				$scope.nextButton = {"enabled" : false};
				$scope.prevButton = {"enabled" : false};
			} else if ($scope.selectedRole.grants.length > 5) {
				$scope.nextButton = {"enabled" : true, "hidden" : false};
				$scope.prevButton = {"enabled" : true, "hidden" : true};
				$scope.grants = $scope.selectedRole.grants.slice(0, 5);
			} else {
				$scope.nextButton = {"enabled" : false};
				$scope.prevButton = {"enabled" : false};
				$scope.grants = $scope.selectedRole.grants;
			}
		}

		$scope.checkedRoles = function () {
			var result = [];
			angular.forEach(checkedRows, function (row) {
				result.push($scope.roleEntities[row]);
			});
			return result;
		}

		$scope.onRoleChecked = function (row) {
			var index = checkedRows.indexOf(row);
			if (index == -1) {
				// Not already selected
				checkedRows.push(row);
			} else {
				// remove row from selection
				checkedRows.splice(index, 1);
			}
		}

		/**
		 * Set the proper icon depending on the row is selected or not.
		 * @param row The row to check
		 * @returns {string} A CSS style class
		 */
		$scope.roleStyleClass = function (row) {
			if (checkedRows.indexOf(row) == -1) {
				return "glyphicon glyphicon-unchecked";
			}
			return "glyphicon glyphicon-check";
		}

		$scope.previousGrantsPage = function() {
			$scope.page--;
			$scope.grants = $scope.selectedRole.grants.slice($scope.page*5, $scope.page*5+5);
			if ($scope.page == 1){
				$scope.grants = $scope.selectedRole.grants.slice($scope.page, $scope.page+5);
				$scope.prevButton = {"enabled" : true, "hidden" : true};
			}
			if ($scope.selectedRole.grants.length > 5) {
				$scope.grants = $scope.selectedRole.grants.slice($scope.page, $scope.page+5);
				$scope.nextButton = {"enabled" : true, "hidden" : false};
			}
		}

		$scope.nextGrantsPage = function() {
			console.log("role.grants for slice:"+$scope.selectedRole.grants.length);
			if ($scope.page*5+5 <= $scope.selectedRole.grants.length) {
				$scope.grants = $scope.selectedRole.grants.slice($scope.page*5, $scope.page*5+5);
				$scope.nextButton = {"enabled" : true, "hidden" : false};
			} else {
				$scope.grants = $scope.selectedRole.grants.slice($scope.page*5, $scope.selectedRole.grants.length+1);
				$scope.nextButton = {"enabled":false, "hidden":true};
			}
			$scope.page++;
			// Enable back button after a click on next...
			$scope.prevButton = {"enabled" : true, "hidden" : false};
		}

		var rolesSaved = function(savedRole) {
			$scope.loadRoles();
			/*
			angular.forEach($scope.roleEntities, function (role) {
				if (role.name == savedRole.name) {
					role = savedRole;
				}
			});
			*/
			onSuccess("OK", "Saved successfully.");
		}
		var onError = function(code, text) {
			toaster.pop("error", "Server Error", "["+code+"] "+text);
		}
		var onSuccess = function(code, text) {
			toaster.pop("success", "Success", "["+code+"] "+text, 2000);
		}

	});

