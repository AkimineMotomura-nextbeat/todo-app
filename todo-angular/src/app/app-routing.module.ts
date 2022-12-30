import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';

import { TodoListComponent } from './todo/todo-list/todo-list.component';
import { TodoDetailComponent } from './todo/todo-detail/todo-detail.component';
import { CategoryListComponent } from './category/category-list/category-list.component';
import { CategoryDetailComponent } from './category/category-detail/category-detail.component';
import { TodoNewComponent } from './todo/todo-new/todo-new.component';
import { CategoryNewComponent } from './category/category-new/category-new.component';

const routes: Routes = [
  { path: '', redirectTo: 'todo/list', pathMatch: 'full'},
  { path: 'todo/list', component: TodoListComponent },
  { path: 'todo/new', component: TodoNewComponent},
  { path: 'todo/:id', component: TodoDetailComponent },
  { path: 'todo/category/list', component: CategoryListComponent },
  { path: 'todo/category/new', component: CategoryNewComponent},
  { path: 'todo/category/:id', component: CategoryDetailComponent }
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
