import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';

import { TodoListComponent } from './todo-list/todo-list.component';
import { TodoDetailComponent } from './todo-detail/todo-detail.component';
import { CategoryListComponent } from './category-list/category-list.component';
import { CategoryDetailComponent } from './category-detail/category-detail.component';

const routes: Routes = [
  { path: '', redirectTo: 'todo/list', pathMatch: 'full'},
  { path: 'todo/list', component: TodoListComponent },
  { path: 'todo/:id', component: TodoDetailComponent },
  { path: 'todo/category/list', component: CategoryListComponent },
  { path: 'todo/category/:id', component: CategoryDetailComponent }
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
